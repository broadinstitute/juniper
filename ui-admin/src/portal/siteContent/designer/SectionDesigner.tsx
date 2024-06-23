import { PortalEnvContext } from 'portal/PortalRouter'
import { HtmlSection } from '@juniper/ui-core'
import { SiteMediaMetadata } from 'api/api'
import { sectionTemplates } from '../sectionTemplates'
import { TitleEditor } from './editors/TitleEditor'
import { BlurbEditor } from './editors/BlurbEditor'
import { ImageEditor } from './editors/ImageEditor'
import { StyleEditor } from './editors/StyleEditor'
import { ParticipationStepsEditor } from './editors/ParticipantStepsEditor'
import { FrequentlyAskedQuestionEditor } from './editors/FrequentlyAskedQuestionEditor'
import { LogoEditor } from './editors/LogoEditor'
import { ButtonEditor } from './editors/ButtonEditor'
import { PhotoBioEditor } from './editors/PhotoBioEditor'
import React from 'react'

/**
 *
 */
export const SectionDesigner = ({ portalEnvContext, section, updateSection, siteMediaList }: {
    portalEnvContext: PortalEnvContext,
    section: HtmlSection, updateSection: (section: HtmlSection) => void, siteMediaList: SiteMediaMetadata[]
}) => {
  const sectionType = section.sectionType
  const sectionTypeConfig = sectionTemplates[sectionType]
  const hasTitle = Object.hasOwnProperty.call(sectionTypeConfig, 'title')
  const hasBlurb = Object.hasOwnProperty.call(sectionTypeConfig, 'blurb')
  const hasSteps = Object.hasOwnProperty.call(sectionTypeConfig, 'steps')
  const hasQuestions = Object.hasOwnProperty.call(sectionTypeConfig, 'questions')
  const hasImage = Object.hasOwnProperty.call(sectionTypeConfig, 'image')
  const hasLogos = Object.hasOwnProperty.call(sectionTypeConfig, 'logos')
  const hasButtons = Object.hasOwnProperty.call(sectionTypeConfig, 'buttons')
  const hasSubGrids = Object.hasOwnProperty.call(sectionTypeConfig, 'subGrids')

  return (
    <div>
      {hasTitle &&
                <TitleEditor section={section} updateSection={updateSection}/>}
      {hasBlurb &&
                <BlurbEditor section={section} updateSection={updateSection}/>}
      {hasImage &&
                <ImageEditor portalEnvContext={portalEnvContext} section={section}
                  updateSection={updateSection} siteMediaList={siteMediaList}/>}
      {/* All sections have a style editor */}
      <StyleEditor section={section} updateSection={updateSection}/>
      {hasSteps &&
                <ParticipationStepsEditor portalEnvContext={portalEnvContext} section={section}
                  updateSection={updateSection} siteMediaList={siteMediaList}/>}
      {hasQuestions &&
                <FrequentlyAskedQuestionEditor section={section} updateSection={updateSection}/>}
      {hasLogos &&
                <LogoEditor portalEnvContext={portalEnvContext} section={section}
                  updateSection={updateSection} siteMediaList={siteMediaList}/>}
      {hasButtons &&
                <ButtonEditor
                  section={section} updateSection={updateSection}/>}
      {hasSubGrids &&
                <PhotoBioEditor portalEnvContext={portalEnvContext} mediaList={siteMediaList}
                  section={section} updateSection={updateSection}/>}
    </div>
  )
}
