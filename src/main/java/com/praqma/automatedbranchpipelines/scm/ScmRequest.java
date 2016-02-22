package com.praqma.automatedbranchpipelines.scm;

/**
 * Model for the values in an SCM request.
 */
public class ScmRequest {

  private static final String CREATE_ACTION = "ADD";

  private static final String DELETE_ACTION = "DELETE";

  private final String scm;

  private final String branch;

  private final String action;

  ScmRequest(String scm, String branch, String action) {
    this.scm = scm;
    this.branch = branch;
    this.action = action;
  }

  boolean isCreate() {
    return CREATE_ACTION.equals(action);
  }

  boolean isDelete() {
    return DELETE_ACTION.equals(action);
  }

  String getAction() {
    return action;
  }

}