import _ from 'lodash'
import React from 'react'

import FrequentlyAskedQuestionsTemplate from './FrequentlyAskedQuestionsTemplate'
import HeroCenteredTemplate from './HeroCenteredTemplate'
import HeroWithImageTemplate from './HeroWithImageTemplate'
import StepOverviewTemplate from './StepOverviewTemplate'
import SocialMediaTemplate from './SocialMediaTemplate'
import { HtmlPage, HtmlSection, SectionConfig, SectionType } from 'api/api'
import RawHtmlTemplate from './RawHtmlTemplate'
import PhotoBlurbGrid from './PhotoBlurbGrid'
import ParticipationDetailTemplate from './ParticipationDetailTemplate'
import NavAndLinkSectionsFooter from './NavAndLinkSectionsFooter'

type TemplateComponent = ({ anchorRef, config, rawContent }:
                            { anchorRef?: string, config: SectionConfig, rawContent: string | null }) => JSX.Element

const templateComponents: Record<SectionType, TemplateComponent> = {
  'FAQ': FrequentlyAskedQuestionsTemplate,
  'HERO_CENTERED': HeroCenteredTemplate,
  'HERO_WITH_IMAGE': HeroWithImageTemplate,
  'SOCIAL_MEDIA': SocialMediaTemplate,
  'STEP_OVERVIEW': StepOverviewTemplate,
  'PHOTO_BLURB_GRID': PhotoBlurbGrid,
  'PARTICIPATION_DETAIL': ParticipationDetailTemplate,
  'RAW_HTML': RawHtmlTemplate,
  'NAV_AND_LINK_SECTIONS_FOOTER': NavAndLinkSectionsFooter
}

/** renders a configured HtmlPage */
export default function HtmlPageView({ page }: { page: HtmlPage }) {
  return <>
    {
      _.map(page.sections, (section: HtmlSection) => <HtmlSectionView section={section} key={section.id}/>)
    }
  </>
}

/** renders a single section by delegating to the appropriate component based on sectionType */
export function HtmlSectionView({ section }: { section: HtmlSection }) {
  const Template = templateComponents[section.sectionType]
  if (!Template) {
    console.warn(`Page configuration error: Unknown section type "${section.sectionType}"`)
    return null
  }
  const parsedConfig: SectionConfig = section.sectionConfig ? JSON.parse(section.sectionConfig) : {}
  return <Template config={parsedConfig} anchorRef={section.anchorRef} rawContent={section.rawContent}/>
}
