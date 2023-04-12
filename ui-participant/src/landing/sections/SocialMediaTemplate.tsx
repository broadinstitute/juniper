import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import { faFacebook, faInstagram, faTwitter } from '@fortawesome/free-brands-svg-icons'
import React from 'react'

import { SectionConfig } from 'api/api'
import { getSectionStyle } from 'util/styleUtils'
import { withValidatedSectionConfig } from 'util/withValidatedSectionConfig'
import { requireOptionalString } from 'util/validationUtils'

import { TemplateComponentProps } from './templateUtils'

type SocialMediaSite = 'Facebook' | 'Instagram' | 'Twitter'

type SocialMediaSiteConfig = {
  domain: string
  icon: IconDefinition
  label: SocialMediaSite
  renderUrl: (args: { domain: string, handle: string }) => string
}

export const socialMediaSites: SocialMediaSiteConfig[] = [
  {
    domain: 'twitter.com',
    icon: faTwitter,
    label: 'Twitter',
    renderUrl: ({ domain, handle }) => `https://${domain}/${handle}`
  },
  {
    domain: 'facebook.com',
    icon: faFacebook,
    label: 'Facebook',
    renderUrl: ({ domain, handle }) => `https://${domain}/${handle}`
  },
  {
    domain: 'instagram.com',
    icon: faInstagram,
    label: 'Instagram',
    renderUrl: ({ domain, handle }) => `https://${domain}/${handle}`
  }
]

type HandleKey = `${Lowercase<SocialMediaSite>}Handle`

type SocialMediaTemplateConfig = {
  [handle in HandleKey]?: string
}

/** Validate that a section configuration object conforms to SocialMediaTemplateConfig */
const validateSocialMediaTemplateConfig = (config: SectionConfig): SocialMediaTemplateConfig => {
  const message = 'Invalid SocialMediaTemplateConfig'
  return socialMediaSites.reduce(
    (acc, { label }) => {
      const key = `${label.toLowerCase()}Handle`
      return { ...acc, [key]: requireOptionalString(config, key, message) }
    },
    {}
  )
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

  return (
    <div id={anchorRef} className="row mx-0" style={getSectionStyle(config)}>
      <div className="hstack justify-content-center gap-2">
        {socialMediaSites.map(site => {
          const handleKey = `${site.label.toLowerCase()}Handle` as HandleKey
          const handle = config[handleKey]
          if (!handle) {
            return null
          } else {
            return (
              <SocialMediaLink
                key={site.label}
                icon={site.icon}
                label={site.label}
                url={site.renderUrl({ domain: site.domain, handle })}
              />
            )
          }
        })}
      </div>
    </div>
  )
}

export default withValidatedSectionConfig(validateSocialMediaTemplateConfig, SocialMediaTemplate)
