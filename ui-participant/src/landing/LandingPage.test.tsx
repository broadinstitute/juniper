import React from 'react'
import { usePortalEnv } from 'providers/PortalProvider'
import {
    HtmlPage,
    HtmlSection,
    LocalSiteContent,
    NavbarItem,
    Portal,
    PortalEnvironment, PortalEnvironmentConfig,
    PortalStudy, SiteContent, Survey
} from "@juniper/ui-core";
import {render, screen} from "@testing-library/react";
import HtmlPageView from "./HtmlPageView";
import LandingPage from "./LandingPage";
import {setupRouterTest} from "../test-utils/router-testing-utils";
import HubPage from "../hub/HubPage";

const emptySiteContent: LocalSiteContent = {
    language: "en",
    navbarItems: [],
    landingPage: {
        title: '',
        path: '',
        sections: []
    },
    navLogoCleanFileName: '',
    navLogoVersion: 1
}

const emptyPortal: Portal = {
    id: '',
    name: '',
    shortcode: '',
    portalEnvironments: [],
    portalStudies: []
}

describe('LandingPage', () => {
    it('handles trivial landing page', () => {
        const { RoutedComponent } =
            setupRouterTest(
                    <LandingPage localContent={emptySiteContent}/>)
        render(RoutedComponent)
        expect(screen.getByText('Join Mailing List')).not.toBeVisible()
    })
})
