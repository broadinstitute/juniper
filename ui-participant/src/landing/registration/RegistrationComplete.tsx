import React from "react";
import {StudyEnvironment} from "api/api";
import {Link} from "react-router-dom";

export default function RegistrationComplete() {
  return <div className="text-center">
    <h3>Thanks</h3>
    <p>Your registration is complete</p>

    <p><Link className="btn btn-primary" to={'/login'}>Login</Link></p>
  </div>

}
