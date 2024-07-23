import React from 'react'

/** keys representating a bit of user-facing documentation */
export enum DocsKey {
  HOME_PAGE = 'HOME_PAGE',
  PREREG_SURVEYS = 'PREREG_SURVEYS',
  SURVEY_EDIT = 'SURVEY_EDIT',
  SEARCH_EXPRESSIONS = 'SEARCH_EXPRESSIONS',
  PROXY_ENROLLMENT = 'PROXY_ENROLLMENT',
  WITHDRAWAL = 'WITHDRAWAL'
}

/** mapping of a docs key to a zendesk page */
const ZENDESK_PAGES: Record<DocsKey, string> = {
  HOME_PAGE: 'https://broad-juniper.zendesk.com',
  // eslint-disable-next-line max-len
  PREREG_SURVEYS: 'https://broad-juniper.zendesk.com/hc/en-us/articles/19922803461915-How-do-I-create-a-Pre-registration-survey-',
  SURVEY_EDIT: 'https://broad-juniper.zendesk.com/hc/en-us/articles/19885357297691-How-do-I-create-and-edit-surveys',
  SEARCH_EXPRESSIONS: 'https://broad-juniper.zendesk.com/hc/en-us/articles/26203593508251-Enrollee-Search-Expressions',
  PROXY_ENROLLMENT: 'https://broad-juniper.zendesk.com/hc/en-us/articles/26099797590555-Proxy-Enrollment',
  WITHDRAWAL: 'https://broad-juniper.zendesk.com/hc/en-us/articles/20172265139995-How-to-withdraw-an-enrollee'
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
