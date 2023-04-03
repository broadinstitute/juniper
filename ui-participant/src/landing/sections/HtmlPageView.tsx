import _ from 'lodash'
import React, { useEffect, useId } from 'react'

import { HtmlPage, HtmlSection, SectionConfig, SectionType } from 'api/api'
import { isPlainObject } from 'util/validationUtils'

import BannerImage from './BannerImage'
import FrequentlyAskedQuestionsTemplate from './FrequentlyAskedQuestionsTemplate'
import HeroCenteredTemplate from './HeroCenteredTemplate'
import HeroWithImageTemplate from './HeroWithImageTemplate'
import StepOverviewTemplate from './StepOverviewTemplate'
import SocialMediaTemplate from './SocialMediaTemplate'
import RawHtmlTemplate from './RawHtmlTemplate'
import PhotoBlurbGrid from './PhotoBlurbGrid'
import ParticipationDetailTemplate from './ParticipationDetailTemplate'
import NavAndLinkSectionsFooter from './NavAndLinkSectionsFooter'
import { TemplateComponent } from './templateUtils'
import { MailingListModal } from 'landing/MailingListModal'

const templateComponents: Record<SectionType, TemplateComponent> = {
  'FAQ': FrequentlyAskedQuestionsTemplate,
  'HERO_CENTERED': HeroCenteredTemplate,
  'HERO_WITH_IMAGE': HeroWithImageTemplate,
  'SOCIAL_MEDIA': SocialMediaTemplate,
  'STEP_OVERVIEW': StepOverviewTemplate,
  'PHOTO_BLURB_GRID': PhotoBlurbGrid,
  'PARTICIPATION_DETAIL': ParticipationDetailTemplate,
  'RAW_HTML': RawHtmlTemplate,
  'NAV_AND_LINK_SECTIONS_FOOTER': NavAndLinkSectionsFooter,
  'BANNER_IMAGE': BannerImage
}

/** renders a configured HtmlPage */
export default function HtmlPageView({ page }: { page: HtmlPage }) {
  const mailingListModalId = useId()

  useEffect(() => {
    const mailingListLinks = document.querySelectorAll<HTMLLinkElement>('a[href="#mailing-list"]')
    Array.from(mailingListLinks).forEach(el => {
      el.dataset.bsToggle = 'modal'
      el.dataset.bsTarget = `#${CSS.escape(mailingListModalId)}`
    })
  }, [])

  return <>
    {
      _.map(page.sections, (section: HtmlSection) => <HtmlSectionView section={section} key={section.id}/>)
    }
    {(page.sections || []).length > 0 && <MailingListModal id={mailingListModalId} />}
  </>
}

/** Get the template component for rendering a section */
const getSectionTemplateComponent = (section: HtmlSection): TemplateComponent => {
  const Template = templateComponents[section.sectionType]
  if (!Template) {
    throw new Error(`Unknown section type "${section.sectionType}"`)
  }
  return Template
}

/** Parse JSON encoded section configuration */
const getSectionTemplateConfig = (section: HtmlSection): SectionConfig => {
  if (!section.sectionConfig) {
    return {}
  }

  let parsedConfig: unknown
  try {
    parsedConfig = JSON.parse(section.sectionConfig)
  } catch {
    throw new Error(`Unable to parse sectionConfig for "${section.sectionType}" section`)
  }

  if (!isPlainObject(parsedConfig)) {
    throw new Error(`Invalid sectionConfig for "${section.sectionType}" section, expected an object`)
  }

  return parsedConfig
}

/** renders a single section by delegating to the appropriate component based on sectionType */
export function HtmlSectionView({ section }: { section: HtmlSection }) {
  try {
    const Template = getSectionTemplateComponent(section)
    const config = getSectionTemplateConfig(section)
    return (
      <Template
        anchorRef={section.anchorRef}
        config={config}
        rawContent={section.rawContent || null}
      />
    )
  } catch (err: unknown) {
    console.warn(`Page configuration error: ${err instanceof Error ? err.message : err}`)
    return null
  }
}
