import React, { useState } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import {
  HtmlPage,
  HtmlSection,
  HtmlSectionView,
  LocalSiteContent,
  NavbarItem,
  SectionType
} from '@juniper/ui-core'
import HtmlSectionEditor from './HtmlSectionEditor'
import { Button } from 'components/forms/Button'
import { sectionTemplates } from './sectionTemplates'
import Api, { SiteMediaMetadata } from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import { filterPriorVersions } from '../media/SiteMediaList'
import { PortalEnvContext } from 'portal/PortalRouter'
import { NavbarPreview } from 'portal/siteContent/NavbarPreview'
import { NavbarSectionEditor } from 'portal/siteContent/NavbarSectionEditor'

type HtmlPageViewProps = {
  localSiteContent: LocalSiteContent
  portalEnvContext: PortalEnvContext
  htmlPage: HtmlPage
  readOnly: boolean
  siteHasInvalidSection: boolean
  setSiteHasInvalidSection: (invalid: boolean) => void
  footerSection?: HtmlSection
  updateFooter: (section?: HtmlSection) => void
  updateNavbarItems: (items: NavbarItem[]) => void
  updatePage: (page: HtmlPage) => void
  useJsonEditor?: boolean
}

/** Enables editing of a given page, showing the config and a preview for each section */
const HtmlPageView = ({
  localSiteContent,
  portalEnvContext,
  htmlPage,
  readOnly,
  siteHasInvalidSection,
  setSiteHasInvalidSection,
  footerSection,
  updateFooter,
  updateNavbarItems,
  updatePage,
  useJsonEditor = true
}: HtmlPageViewProps) => {
  const [mediaList, setMediaList] = useState<SiteMediaMetadata[]>([])

  useLoadingEffect(async () => {
    const result = await Api.getPortalMedia(portalEnvContext.portal.shortcode)
    /** Only show the most recent version of a given image in the list */
    setMediaList(filterPriorVersions(result))
  }, [portalEnvContext.portal.shortcode, portalEnvContext.portalEnv.environmentName])

  const DEFAULT_SECTION_TYPE = {
    id: '',
    sectionType: 'HERO_WITH_IMAGE' as SectionType,
    sectionConfig: JSON.stringify(sectionTemplates['HERO_WITH_IMAGE'])
  }

  //Inserts a new HtmlSection at the specified index on the page
  const insertNewSection = (sectionIndex: number, newSection: HtmlSection) => {
    const newSectionArray = [...htmlPage.sections]
    newSectionArray.splice(sectionIndex, 0, newSection)
    htmlPage = {
      ...htmlPage,
      sections: newSectionArray
    }
    updatePage(htmlPage)
  }

  const removeSection = (sectionIndex: number) => () => {
    const newSectionArray = [...htmlPage.sections]
    newSectionArray.splice(sectionIndex, 1)
    htmlPage = {
      ...htmlPage,
      sections: newSectionArray
    }
    //When the site content is invalid, users can only delete the invalid section. So it's safe to reset this flag
    setSiteHasInvalidSection(false)

    updatePage(htmlPage)
  }

  const moveSection = (sectionIndex: number) => (direction: 'up' | 'down') => {
    if (sectionIndex === 0 && direction === 'up') { return }
    const newSectionArray = [...htmlPage.sections]
    const sectionToMove = newSectionArray[sectionIndex]
    newSectionArray.splice(sectionIndex, 1)
    if (direction === 'up') {
      newSectionArray.splice(sectionIndex - 1, 0, sectionToMove)
    } else {
      newSectionArray.splice(sectionIndex + 1, 0, sectionToMove)
    }
    htmlPage = {
      ...htmlPage,
      sections: newSectionArray
    }
    updatePage(htmlPage)
  }

  const updateSection = (sectionIndex: number) => (updatedSection: HtmlSection) => {
    const newSection = {
      ...htmlPage.sections[sectionIndex],
      sectionType: updatedSection.sectionType,
      sectionConfig: updatedSection.sectionConfig,
      rawContent: updatedSection.rawContent
    }
    const newSectionArray = [...htmlPage.sections]
    newSectionArray[sectionIndex] = newSection
    htmlPage = {
      ...htmlPage,
      sections: newSectionArray
    }
    updatePage(htmlPage)
  }

  const renderAddSectionButton = (sectionIndex: number) => {
    return <div className="col-md-12 my-2" style={{ backgroundColor: '#eee' }}>
      <Button variant="secondary"
        aria-label={'Insert a blank section'}
        tooltip={'Insert a blank section'}
        disabled={readOnly || siteHasInvalidSection}
        onClick={() => insertNewSection(sectionIndex, DEFAULT_SECTION_TYPE)}>
        <FontAwesomeIcon icon={faPlus}/> Insert section
      </Button>
    </div>
  }

  return <div>
    {localSiteContent.navbarItems && <div className="row g-0">
      <div className="col-md-4 px-2 pb-2 mb-2">
        <NavbarSectionEditor
          updateNavbarItems={updateNavbarItems}
          localSiteContent={localSiteContent}
        />
      </div>
      <div className="col-md-8">
        <NavbarPreview
          portal={portalEnvContext.portal}
          portalEnv={portalEnvContext.portalEnv}
          localContent={localSiteContent}
        />
      </div>
    </div>}
    { renderAddSectionButton(0) }
    {htmlPage.sections.map((section, index) => {
      return <div key={`${section.id}-${index}`} className="row g-0">
        <div className="col-md-4 px-2 pb-2">
          <HtmlSectionEditor portalEnvContext={portalEnvContext}
            updateSection={updateSection(index)} setSiteHasInvalidSection={setSiteHasInvalidSection}
            moveSection={moveSection(index)} removeSection={removeSection(index)}
            allowTypeChange={section.id === ''}
            siteHasInvalidSection={siteHasInvalidSection} section={section} readOnly={readOnly}
            useJsonEditor={useJsonEditor} siteMediaList={mediaList}/>
        </div>
        <div className="col-md-8">
          <HtmlSectionView section={section}/>
        </div>
        { renderAddSectionButton(index + 1) }
      </div>
    })}
    { footerSection && <div className="row g-0">
      <div className="col-md-4 px-2 pb-2 mb-2">
        <HtmlSectionEditor portalEnvContext={portalEnvContext}
          setSiteHasInvalidSection={setSiteHasInvalidSection} allowTypeChange={false}
          //These are undefined because we do not allow the user to move or remove the footer section
          moveSection={undefined} removeSection={() => updateFooter(undefined)}
          siteHasInvalidSection={siteHasInvalidSection} section={footerSection}
          useJsonEditor={useJsonEditor}
          updateSection={updateFooter} readOnly={readOnly} siteMediaList={mediaList}/>
      </div>
      <div className="col-md-8">
        <HtmlSectionView section={footerSection}/>
      </div>
    </div> }
    { !footerSection && <div className="col-md-12 my-2" style={{ backgroundColor: '#eee' }}>
      <Button variant="secondary"
        aria-label={'Insert a footer'}
        tooltip={'Insert a footer'}
        disabled={readOnly || siteHasInvalidSection}
        onClick={() => updateFooter({
          id: '',
          sectionType: 'LINK_SECTIONS_FOOTER' as SectionType,
          sectionConfig: JSON.stringify(sectionTemplates['LINK_SECTIONS_FOOTER'])
        })}>
        <FontAwesomeIcon icon={faPlus}/> Insert footer
      </Button>
    </div>}
  </div>
}

export default HtmlPageView
