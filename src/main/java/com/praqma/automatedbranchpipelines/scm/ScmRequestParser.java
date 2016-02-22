package com.praqma.automatedbranchpipelines.scm;

import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Parses HTTP requests from SCM systems.
 * <p>
 *
 * The request body should be JSON. It should always contain these entries:
 * <ul>
 *   <li>scm : The SCM type, for example "git"</li>
 *   <li>branch : The branch name, for example "feature/1337-coolfeature"</li>
 * </ul>
 * <p>
 *
 * Details of a Git/Stash/Bitbucket Server request:
 * <ul>
 *  <li>scm is "git"</li>
 *  <li>action can be "ADD", "UPDATE" or "DELETE". ADD signals branch creation, DELETE
 *      signals branch deletion</li>
 * </ul>
 */
class ScmRequestParser {

  /**
   * Reads a JSON request body.
   *
   * @throws ScmRequestException if the request body is invalid
   */
  static ScmRequest parse(InputStream requestBody) throws ScmRequestException {
	  try (JsonReader reader = Json.createReader(requestBody)) {
      JsonObject json;
      try {
        json = reader.readObject();
      } catch (Exception e) {
        throw new ScmRequestException("Invalid JSON request body", e);
      }

      String scm = readKey("scm", json);
      String branch = readKey("branch", json);
      String action = readKey("action", json);
      return new ScmRequest(scm, branch, action);
    }
  }

  private static String readKey(String key, JsonObject json) throws ScmRequestException {
    if (!json.containsKey(key)) {
      throw new ScmRequestException("JSON request body does not contain key '" + key + "''");
    }
    return json.getString(key);
  }
}