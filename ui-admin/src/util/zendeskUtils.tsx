import React from 'react'

/** keys representating a bit of user-facing documentation */
export enum DocsKey {
    HOME_PAGE = 'HOME_PAGE',
    PREREG_SURVEYS = 'PREREG_SURVEYS'
}

/** mapping of a docs key to a zendesk page */
const ZENDESK_PAGES: Record<DocsKey, string> = {
  HOME_PAGE: 'https://broad-juniper.zendesk.com',
  // eslint-disable-next-line max-len
  PREREG_SURVEYS: 'https://broad-juniper.zendesk.com/hc/en-us/articles/19922803461915-How-do-I-create-a-Pre-registration-survey-'
}

/** gets the url for a given documentation key */
export const zendeskUrl = (key: DocsKey): string => {
  // typescript should make sure all keys are valid, but just in case, default to the home page
  return ZENDESK_PAGES[key] ?? ZENDESK_PAGES['HOME_PAGE']
}

/** renders basic html link to our documentation to open in a new page/tab */
export const ZendeskLink = ({ doc, className, children }:
                                {doc: DocsKey, className?: string, children: React.ReactNode}) => {
  return <a href={zendeskUrl(doc)} className={className} target="_blank">{children}</a>
}
