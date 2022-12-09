import React from "react";
import {HtmlPage, PortalEnvironment} from "api/api";

import {Outlet} from "react-router-dom";

function LandingPageView({ homePage, currentEnv }: {homePage: HtmlPage, currentEnv: PortalEnvironment}) {
  return <div className="LandingPage">
    <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0">
      <div>
      </div>
      <div className="flex-grow-1">
        { homePage.sections.map(section => <div>
          Raw Section<br/>
          <div dangerouslySetInnerHTML={{__html: section.rawContent}}>

          </div>
        </div>)}
      </div>
    </div>
  </div>
}

export default LandingPageView
