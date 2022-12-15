import React from "react";
import {PortalEnvironment} from "api/api";

function Footer({currentEnv}: {currentEnv: PortalEnvironment}) {
  return <footer className="mt-auto ">
    <div className="container bg-white">
      <div className="row">
        <div className="col-12 d-flex justify-content-center">
          <a href="#" className="m-2 me-6">Terms of use</a>  <a href="#" className="m-2 ms-6">Privacy policy</a>
        </div>
      </div>
    </div>
  </footer>
}

export default Footer
