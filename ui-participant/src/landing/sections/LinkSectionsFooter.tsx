import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faEnvelope } from '@fortawesome/free-regular-svg-icons'
import { faFacebook, faInstagram, faTwitter } from '@fortawesome/free-brands-svg-icons'
import _ from 'lodash'
import React from 'react'

import { SectionConfig } from 'api/api'
import { withValidatedSectionConfig } from 'util/withValidatedSectionConfig'
import { requireOptionalArray, requirePlainObject, requireString } from 'util/validationUtils'

import { ButtonConfig, ConfiguredLink, validateButtonConfig } from 'landing/ConfiguredButton'
import { TemplateComponentProps } from './templateUtils'

type LinkSectionsFooterConfig = {
  itemSections?: ItemSection[]
}

type ItemSection = {
  title: string,
  items: ButtonConfig[]
}

const validateItemSection = (config: unknown): ItemSection => {
  const message = 'Invalid LinkSectionsFooterConfig: Invalid itemSection'
  const configObj = requirePlainObject(config, message)
  const items = requireOptionalArray(configObj, 'items', validateButtonConfig, message)
  const title = requireString(configObj, 'title', message)
  return { items, title }
}

/** Validate that a section configuration object conforms to LinkSectionsFooterConfig */
const validateLinkSectionsFooterConfig = (config: SectionConfig): LinkSectionsFooterConfig => {
  const message = 'Invalid LinkSectionsFooterConfig'
  const itemSections = requireOptionalArray(config, 'itemSections', validateItemSection, message)
  return { itemSections }
}

const getIcon = (item: ButtonConfig) => {
  const style = { color: 'var(--brand-color)', width: 20, marginRight: '0.5rem' }
  if (item.type === 'mailingList') {
    return <FontAwesomeIcon icon={faEnvelope} style={style} />
  }

  if (!item.type) {
    if (item.href.startsWith('https://facebook.com/')) {
      return <FontAwesomeIcon icon={faFacebook} style={style} />
    }
    if (item.href.startsWith('https://instagram.com/')) {
      return <FontAwesomeIcon icon={faInstagram} style={style} />
    }
    if (item.href.startsWith('https://twitter.com/')) {
      return <FontAwesomeIcon icon={faTwitter} style={style} />
    }
  }

  return null
}

type LinkSectionsFooterProps = TemplateComponentProps<LinkSectionsFooterConfig>

/** renders a footer-style section */
export function LinkSectionsFooter(props: LinkSectionsFooterProps) {
  const { config } = props

  return <>
    <div className="row mx-0 justify-content-between">
      {_.map(config.itemSections, (section, index) =>
        <div key={index} className="col-12 col-sm-6 col-lg-auto">
          <h2 className="h6 fw-bold mb-4">{section.title}</h2>
          <ul className="list-unstyled">
            {_.map(section.items, (item, index) => {
              return (
                <li key={index} className="mb-3">
                  {getIcon(item)}
                  <ConfiguredLink config={item} />
                </li>
              )
            })}
          </ul>
        </div>
      )}
    </div>
  </>
}

export default withValidatedSectionConfig(validateLinkSectionsFooterConfig, LinkSectionsFooter)
