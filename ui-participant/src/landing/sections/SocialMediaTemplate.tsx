import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import { faFacebook, faInstagram, faTwitter } from '@fortawesome/free-brands-svg-icons'
import React from 'react'

import { SectionConfig } from 'api/api'
import { getSectionStyle } from 'util/styleUtils'
import { withValidatedSectionConfig } from 'util/withValidatedSectionConfig'
import { requireOptionalString } from 'util/validationUtils'

import { TemplateComponentProps } from './templateUtils'

type SocialMediaTemplateConfig = {
  facebookHandle?: string, // Facebook handle
  instagramHandle?: string, // Instagram handle
  twitterHandle?: string, // Twitter handle
}

/** Validate that a section configuration object conforms to SocialMediaTemplateConfig */
const validateSocialMediaTemplateConfig = (config: SectionConfig): SocialMediaTemplateConfig => {
  const message = 'Invalid SocialMediaTemplateConfig'
  const facebookHandle = requireOptionalString(config, 'facebookHandle', message)
  const instagramHandle = requireOptionalString(config, 'instagramHandle', message)
  const twitterHandle = requireOptionalString(config, 'twitterHandle', message)
  return {
    facebookHandle,
    instagramHandle,
    twitterHandle
  }
}


type SocialMediaLinkProps = {
  icon: IconDefinition
  label: string
  url: string
}

const SocialMediaLink = (props: SocialMediaLinkProps) => {
  const { icon, label, url } = props
  return (
    <a className="px-2" href={url} rel="noreferrer" target="_blank">
      <span className="visually-hidden">{label}</span>
      <FontAwesomeIcon icon={icon} style={{ height: '3rem' }} />
    </a>
  )
}

type SocialMediaTemplateProps = TemplateComponentProps<SocialMediaTemplateConfig>

/**
 * Template for a hero with social media links.
 */
function SocialMediaTemplate(props: SocialMediaTemplateProps) {
  const { anchorRef, config } = props
  const {
    facebookHandle,
    instagramHandle,
    twitterHandle
  } = config

  return (
    <div id={anchorRef} className="row mx-0 py-5" style={getSectionStyle(config)}>
      <div className="hstack justify-content-center gap-2">
        {twitterHandle && (
          <SocialMediaLink
            icon={faTwitter}
            label="Twitter"
            url={`https://twitter.com/${twitterHandle}`}
          />
        )}
        {facebookHandle && (
          <SocialMediaLink
            icon={faFacebook}
            label="Facebook"
            url={`https://facebook.com/${facebookHandle}`}
          />
        )}
        {instagramHandle && (
          <SocialMediaLink
            icon={faInstagram}
            label="Instagram"
            url={`https://instagram.com/${instagramHandle}`}
          />
        )}
      </div>
    </div>
  )
}

export default withValidatedSectionConfig(validateSocialMediaTemplateConfig, SocialMediaTemplate)
