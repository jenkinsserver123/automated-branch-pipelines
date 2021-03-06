/**
 * A very basic build pipeline with commit, test and release jobs.
 *
 * Assumes these Jenkins job build parameters:
 * BRANCH - branch, used in job names and scm git branch specifier. May contain "/"
 * which are replaced with "_" in job names.
 * PIPELINE - a comma separated list of jobs constituting the desired pipeline
 */

import javaposse.jobdsl.dsl.DslException

/**
 * Get a mandatory Jenkins build parameter passed in by the Parameterized Build plugin.
 */
def getBuildParameter = { key ->
  def value = binding.variables.get(key)
  if (!value) {
    throw new DslException("Build parameter ${key} is missing")
  }
  return value
}

/**
 * Get a formatted job name identified by branch.
 */
def getJobName = { jobPrefix, branch ->
  def branchForJobName = branch.replaceAll("/", "_")
  return "${jobPrefix}_${branchForJobName}"
}

/**
 * Determine if a job is part of a pipeline based on the job name.
 */
def isJobInPipeline = { jobName, pipeline ->
  return pipeline.any { jobName.startsWith(it) }
}

def branch = getBuildParameter('BRANCH')
def pipeline = getBuildParameter('PIPELINE').tokenize(',')

// Branch specifier for git scm
def branchSpecifier = "refs/heads/${branch}"

def commitJobName = getJobName('commit', branch)
def testJobName = getJobName('test', branch)
def releaseJobName = getJobName('release', branch)

// A map from job names to closures defining the jobs
jobs = [
  commitJobName : {
    job(commitJobName) {
      description 'Build source code and run unit tests'

      scm {
        github('martinmosegaard/test-automated-branch-pipelines', branchSpecifier)
      }

      steps {
        shell('echo Generated by the commit job > commit-artifact.txt')
      }

      publishers {
        archiveArtifacts {
          pattern('commit-artifact.txt')
        }
      }
    }
  },

  testJobName : {
    job(testJobName) {
      description 'Acceptance tests'

      steps {
        copyArtifacts(commitJobName) {
          includePatterns('commit-artifact.txt')
        }
        shell('cat commit-artifact.txt')
      }
    }
  },

  releaseJobName : {
    job(releaseJobName) {
      description 'Package a release'

      steps {
        copyArtifacts(commitJobName) {
          includePatterns('commit-artifact.txt')
        }
        shell('cp commit-artifact.txt release-artifact.txt')
      }

      publishers {
        archiveArtifacts {
          pattern('release-artifact.txt')
        }
      }
    }
  },
]

// Create the jobs that are part of the pipeline
jobs.each { jobName, jobClosure ->
  if (isJobInPipeline(jobName, pipeline)) {
    jobClosure()
  }
}
