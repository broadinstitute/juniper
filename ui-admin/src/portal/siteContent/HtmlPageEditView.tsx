import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { HtmlPage, HtmlSection, HtmlSectionView, SectionType } from '@juniper/ui-core'
import HtmlSectionEditor from './HtmlSectionEditor'
import { Button } from 'components/forms/Button'
import { sectionTemplates } from './sectionTemplates'

type HtmlPageViewProps = {
  htmlPage: HtmlPage
  readOnly: boolean
  siteInvalid: boolean
  setSiteInvalid: (invalid: boolean) => void
  updatePage: (page: HtmlPage) => void
}

/** Enables editing of a given page, showing the config and a preview for each section */
const HtmlPageView = ({ htmlPage, readOnly, siteInvalid, setSiteInvalid, updatePage }: HtmlPageViewProps) => {
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

  const renderAddSectionButton = (sectionIndex: number) => {
    return <div className="col-md-12 my-2" style={{ backgroundColor: '#eee' }}>
      <Button variant="secondary"
        aria-label={'Insert a blank section'}
        tooltip={'Insert a blank section'}
        disabled={readOnly || siteInvalid}
        onClick={() => insertNewSection(sectionIndex, DEFAULT_SECTION_TYPE)}>
        <FontAwesomeIcon icon={faPlus}/> Insert section
      </Button>
    </div>
  }

  return <div>
    { renderAddSectionButton(0) }
    {htmlPage.sections.map((section, index) => {
      return <div key={`${section.id}-${index}`} className="row g-0">
        <div className="col-md-4 p-2">
          <HtmlSectionEditor updatePage={updatePage} htmlPage={htmlPage} setSiteInvalid={setSiteInvalid}
            siteInvalid={siteInvalid} section={section} sectionIndex={index} readOnly={readOnly}/>
        </div>
        <div className="col-md-8">
          <HtmlSectionView section={section}/>
        </div>
        { renderAddSectionButton(index + 1) }
      </div>
    })}
  </div>
}

export default HtmlPageView
