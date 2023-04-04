import _ from 'lodash'
import React from 'react'
import { Link } from 'react-router-dom'

import { SectionConfig } from 'api/api'
import { withValidatedSectionConfig } from 'util/withValidatedSectionConfig'
import { requireOptionalArray, requirePlainObject, requireString } from 'util/validationUtils'

import { ButtonConfig, validateButtonConfig } from 'landing/ConfiguredButton'
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

type LinkSectionsFooterProps = TemplateComponentProps<LinkSectionsFooterConfig>

/** renders a footer-style section */
export function LinkSectionsFooter(props: LinkSectionsFooterProps) {
  const { config } = props

  return <>
    <div className="d-flex justify-content-center py-5">
      <div className="col-lg-8">
        <div className="row justify-content-between mx-0">
          {_.map(config.itemSections, (section, index) =>
            <div key={index} className="col-12 col-sm-6 col-lg-auto">
              <h2 className="h6 fw-bold mb-4">{section.title}</h2>
              <ul className="list-unstyled">
                {_.map(section.items, (item, index) => {
                  return (
                    <li key={index} className="mb-3">
                      <FooterItem item={item} />
                    </li>
                  )
                })}
              </ul>
            </div>
          )}
        </div>
      </div>
    </div>
  </>
}

export default withValidatedSectionConfig(validateLinkSectionsFooterConfig, LinkSectionsFooter)

/**
 * Renders an individual item (e.g. a link) for the footer.
 */
function FooterItem({ item }: { item: ButtonConfig }) {
  if (item.type === 'join') {
    const to = item.studyShortcode ? `/studies/${item.studyShortcode}/join` : '/join'
    return <Link to={to}>{item.text}</Link>
  } else if (item.type === 'mailingList') {
    return <a href="#mailing-list">{item.text}</a>
  } else if (item.type === 'internalLink') {
    return <Link to={item.href}>{item.text}</Link>
  }
  return <a href={item.href} rel="noreferrer">{item.text}</a>
}
