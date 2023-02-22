import _ from 'lodash'
import React from 'react'

import FrequentlyAskedQuestionsTemplate from './FrequentlyAskedQuestionsTemplate'
import HeroCenteredTemplate from './HeroCenteredTemplate'
import HeroLeftWithImageTemplate from './HeroLeftWithImageTemplate'
import HeroRightWithImageTemplate from './HeroRightWithImageTemplate'
import StepOverviewTemplate from './StepOverviewTemplate'
import SocialMediaTemplate from './SocialMediaTemplate'
import { HtmlPage, HtmlSection, SectionConfig } from 'api/api'
import RawHtmlTemplate from './RawHtmlTemplate'
import PhotoBlurbGrid from './PhotoBlurbGrid'
import ParticipationDetailTemplate from './ParticipationDetailTemplate'

type TemplateComponent = ({ config, rawContent }: { config: SectionConfig, rawContent: string | null }) => JSX.Element

const templateComponents: { [index: string]: TemplateComponent } = {
  'FAQ': FrequentlyAskedQuestionsTemplate,
  'HERO_CENTERED': HeroCenteredTemplate,
  'HERO_LEFT_WITH_IMAGE': HeroLeftWithImageTemplate,
  'HERO_RIGHT_WITH_IMAGE': HeroRightWithImageTemplate,
  'SOCIAL_MEDIA': SocialMediaTemplate,
  'STEP_OVERVIEW': StepOverviewTemplate,
  'PHOTO_BLURB_GRID': PhotoBlurbGrid,
  'PARTICIPATION_DETAIL': ParticipationDetailTemplate,
  'RAW_HTML': RawHtmlTemplate
}

/** renders a configured HtmlPage */
export default function HtmlPageView({ page }: { page: HtmlPage }) {
  return <>
    {
      _.map(page.sections, (section: HtmlSection) => {
        const key = section.id
        const Template = templateComponents[section.sectionType]

        const parsedConfig: SectionConfig = section.sectionConfig ? JSON.parse(section.sectionConfig) : {}
        return <Template key={key} config={parsedConfig} rawContent={section.rawContent}/>
      })
    }
  </>
}
