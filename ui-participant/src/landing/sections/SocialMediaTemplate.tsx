import React from 'react'

import { SectionConfig } from 'api/api'
import { getSectionStyle } from 'util/styleUtils'
import { withValidatedSectionConfig } from 'util/withValidatedSectionConfig'
import { requireOptionalString } from 'util/validationUtils'

import PearlImage from '../PearlImage'

type SocialMediaTemplateConfig = {
  facebookHref?: string, // URL of Facebook page
  instagramHref?: string, // URL of Instagram page
  twitterHref?: string, // URL of Twitter page
}

/** Validate that a section configuration object conforms to SocialMediaTemplateConfig */
const validateSocialMediaTemplateConfig = (config: SectionConfig): SocialMediaTemplateConfig => {
  const message = 'Invalid SocialMediaTemplateConfig'
  const facebookHref = requireOptionalString(config, 'facebookHref', message)
  const instagramHref = requireOptionalString(config, 'instagramHref', message)
  const twitterHref = requireOptionalString(config, 'twitterHref', message)
  return {
    facebookHref,
    instagramHref,
    twitterHref
  }
}

type SocialMediaTemplateProps = {
  anchorRef?: string
  config: SocialMediaTemplateConfig
}

/**
 * Template for a hero with social media links.
 * TODO -- implement images
 */
function SocialMediaTemplate(props: SocialMediaTemplateProps) {
  const { anchorRef, config } = props
  const {
    facebookHref,
    instagramHref,
    twitterHref
  } = config

  return <div id={anchorRef} className="container py-5" style={getSectionStyle(config)}>
    <div className="d-flex justify-content-center mt-5 mb-4">
      {twitterHref &&
        <PearlImage image={{ cleanFileName: 'twitter.png', version: 1, alt: 'Twitter' }}
          className="m-3" style={{ width: '56px' }}/>
      }
      {facebookHref &&
        <PearlImage image={{ cleanFileName: 'facebook.png', version: 1, alt: 'Facebook' }}
          className="m-3" style={{ width: '54px' }}/>
      }
      {instagramHref &&
        <PearlImage image={{ cleanFileName: 'instagram.png', version: 1, alt: 'Instagram' }}
          className="m-3" style={{ width: '49px' }}/>
      }
    </div>
  </div>
}

export default withValidatedSectionConfig(validateSocialMediaTemplateConfig, SocialMediaTemplate)
