import React from 'react'
import _ from 'lodash'
import LandingNavbar from '../LandingNavbar'


type NavAndLinkSectionsFooterProps = {
  background?: string, // background CSS style (e.g. `linear-gradient(...)`)
  includeNavbar?: boolean,
  itemSections?: ItemSection[]
}

type ItemSection = {
  title: string,
  items: FooterItem[]
}

type FooterItem = {
  label: string,
  itemType: string,
  externalLink: string
}

/** renders a footer-style section */
export default function NavAndLinkSectionsFooter({ config }: { config: NavAndLinkSectionsFooterProps }) {
  return <>
    {config.includeNavbar && <LandingNavbar/>}
    <div className="d-flex justify-content-center py-3">
      <div className="col-lg-8">
        {_.map(config.itemSections, (section, index) =>
          <div key={index} className="d-inline-block pb-3 px-2">
            <h6>{section.title}</h6>
            <div>
              {_.map(section.items, (item, index) => <FooterItem item={item} key={index}/>)}
            </div>
          </div>
        )}
      </div>
    </div>
  </>
}

/**
 * renders an individual item (e.g. a link) for the footer.  this shares a bit of functionality with CustomNavLink in
 * NavbarItem.tsx.  When the MAILING_LIST is implemented, it should be shared.
 */
function FooterItem({ item }: { item: FooterItem }) {
  /** will eventually popup a modal allowing email address entry */
  function mailingList(item: FooterItem) {
    alert(`mailing list ${item.label} - not implemented`)
  }

  if (item.itemType === 'MAILING_LIST') {
    return <a role="button" className=" me-3" onClick={() => mailingList(item)}>{item.label}</a>
  } else if (item.itemType === 'EXTERNAL') {
    return <a href={item.externalLink} className=" me-3" target="_blank">{item.label}</a>
  }
  return <></>
}
