import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { HtmlPage, HtmlSection, HtmlSectionView } from '@juniper/ui-core'
import HtmlSectionEditor from './HtmlSectionEditor'
import { Button } from 'components/forms/Button'

type HtmlPageViewProps = {
  htmlPage: HtmlPage
  readOnly: boolean
  updatePage: (page: HtmlPage) => void
}

/** Enables editing of a given page, showing the config and a preview for each section */
const HtmlPageView = ({ htmlPage, updatePage, readOnly }: HtmlPageViewProps) => {
  const updateSection = (sectionIndex: number, updatedSection: HtmlSection) => {
    try {
      JSON.parse(updatedSection.sectionConfig ?? '{}')
    } catch (e) {
      // for now, we just don't allow changing the object structure itself -- just plain text edits
      return
    }

    const newSection = {
      ...htmlPage.sections[sectionIndex],
      sectionType: updatedSection.sectionType,
      sectionConfig: updatedSection.sectionConfig
    }
    const newSectionArray = [...htmlPage.sections]
    newSectionArray[sectionIndex] = newSection
    htmlPage = {
      ...htmlPage,
      sections: newSectionArray
    }
    updatePage(htmlPage)
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

  return <div>
    {htmlPage.sections.map((section, index) => {
      return <div key={index} className="row">
        <div className="col-md-4 p-2">
          <HtmlSectionEditor
            section={section} sectionIndex={index} readOnly={readOnly} updateSection={updateSection}/>
        </div>
        <div className="col-md-8">
          <HtmlSectionView section={section}/>
        </div>
        <div className="col-md-12 my-2" style={{ backgroundColor: '#eee' }}>
          <Button variant="secondary"
            aria-label={'Insert a blank section'}
            tooltip={'Insert a blank section'}
            disabled={readOnly}
            onClick={() => insertNewSection(index + 1, { id: '', sectionType: 'HERO_WITH_IMAGE' })}>
            <FontAwesomeIcon icon={faPlus}/> Insert section
          </Button>
        </div>
      </div>
    })}
  </div>
}

export default HtmlPageView
