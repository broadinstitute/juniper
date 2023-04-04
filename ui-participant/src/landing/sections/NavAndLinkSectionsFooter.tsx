import React from 'react'
import _ from 'lodash'

import { SectionConfig } from 'api/api'
import { withValidatedSectionConfig } from 'util/withValidatedSectionConfig'
import { requireOptionalArray, requirePlainObject, requireString } from 'util/validationUtils'

import LandingNavbar from '../LandingNavbar'

import { TemplateComponentProps } from './templateUtils'

type NavAndLinkSectionsFooterConfig = {
  includeNavbar?: boolean,
  itemSections?: ItemSection[]
}

type ItemSection = {
  title: string,
  items: FooterItem[]
}

type ExternalLinkFooterItem = {
  label: string
  itemType: 'EXTERNAL'
  externalLink: string
}

type MailingListFooterItem = {
  label: string
  itemType: 'MAILING_LIST'
}

type FooterItem = ExternalLinkFooterItem | MailingListFooterItem

const validateFooterItem = (config: unknown): FooterItem => {
  const message = 'Invalid NavAndLinkSectionsFooterConfig: Invalid item'
  const configObj = requirePlainObject(config, message)
  const label = requireString(configObj, 'label', message)
  const itemType = requireString(configObj, 'itemType', message)
  if (!(itemType === 'EXTERNAL' || itemType === 'MAILING_LIST')) {
    throw new Error(`${message}: itemType must be one of "EXTERNAL" or "MAILING_LIST"`)
  }

  if (itemType === 'EXTERNAL') {
    const externalLink = requireString(configObj, 'externalLink', message)
    return { label, itemType, externalLink }
  } else {
    return { label, itemType }
  }
}

const validateItemSection = (config: unknown): ItemSection => {
  const message = 'Invalid NavAndLinkSectionsFooterConfig: Invalid itemSection'
  const configObj = requirePlainObject(config, message)
  const items = requireOptionalArray(configObj, 'items', validateFooterItem, message)
  const title = requireString(configObj, 'title', message)
  return { items, title }
}

/** Validate that a section configuration object conforms to NavAndLinkSectionsFooterConfig */
const validateNavAndLinkSectionsFooterConfig = (config: SectionConfig): NavAndLinkSectionsFooterConfig => {
  const message = 'Invalid NavAndLinkSectionsFooterConfig'
  const includeNavbar = !!config.includeNavbar
  const itemSections = requireOptionalArray(config, 'itemSections', validateItemSection, message)
  return { includeNavbar, itemSections }
}

type NavAndLinkSectionsFooterProps = TemplateComponentProps<NavAndLinkSectionsFooterConfig>

/** renders a footer-style section */
export function NavAndLinkSectionsFooter(props: NavAndLinkSectionsFooterProps) {
  const { config } = props

  return <>
    {config.includeNavbar && <LandingNavbar aria-label="Secondary" />}
    <div className="d-flex justify-content-center py-3">
      <div className="col-lg-8">
        {_.map(config.itemSections, (section, index) =>
          <div key={index} className="d-inline-block pb-3 px-2">
            <h2 className="h6">{section.title}</h2>
            <div>
              {_.map(section.items, (item, index) => <FooterItem item={item} key={index}/>)}
            </div>
          </div>
        )}
      </div>
    </div>
  </>
}

export default withValidatedSectionConfig(validateNavAndLinkSectionsFooterConfig, NavAndLinkSectionsFooter)

/**
 * renders an individual item (e.g. a link) for the footer.  this shares a bit of functionality with CustomNavLink in
 * NavbarItem.tsx.  When the MAILING_LIST is implemented, it should be shared.
 */
function FooterItem({ item }: { item: FooterItem }) {
  if (item.itemType === 'MAILING_LIST') {
    return <a className="me-3" href="#mailing-list">{item.label}</a>
  } else if (item.itemType === 'EXTERNAL') {
    return <a href={item.externalLink} className="me-3" target="_blank">{item.label}</a>
  }
  return null
}
