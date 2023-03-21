import React from 'react'

import PearlImage from 'util/PearlImage'
import { getSectionStyle } from 'util/styleUtils'

type SocialMediaTemplateConfig = {
  facebookHref?: string, // URL of Facebook page
  instagramHref?: string, // URL of Instagram page
  twitterHref?: string, // URL of Twitter page
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

export default SocialMediaTemplate
