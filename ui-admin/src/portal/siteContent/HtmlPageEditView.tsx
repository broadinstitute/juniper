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
    <div className="col-md-12 my-2" style={{ backgroundColor: '#eee' }}>
      <Button variant="secondary"
        aria-label={'Insert a blank section'}
        tooltip={'Insert a blank section'}
        disabled={readOnly}
        onClick={() => insertNewSection(0, { id: '', sectionType: 'HERO_WITH_IMAGE' })}>
        <FontAwesomeIcon icon={faPlus}/> Insert section
      </Button>
    </div>
    {htmlPage.sections.map((section, index) => {
      return <div key={`${section.id}-${index}`} className="row g-0">
        <div className="col-md-4 p-2">
          <HtmlSectionEditor updatePage={updatePage} htmlPage={htmlPage}
            section={section} sectionIndex={index} readOnly={readOnly}/>
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
