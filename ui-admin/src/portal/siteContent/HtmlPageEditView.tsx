import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { HtmlPage, HtmlSection, HtmlSectionView } from '@juniper/ui-core'
import HtmlSectionEditor from './HtmlSectionEditor'

type HtmlPageViewProps = {
  htmlPage: HtmlPage
  readOnly: boolean
  updatePage: (page: HtmlPage) => void
}

/** Enables editing of a given page, showing the config and a preview for each section */
const HtmlPageView = ({ htmlPage, updatePage, readOnly }: HtmlPageViewProps) => {
  const updateSectionConfig = (sectionIndex: number, newConfig: string) => {
    try {
      JSON.parse(newConfig)
    } catch (e) {
      // for now, we just don't allow changing the object structure itself -- just plain text edits
      return
    }

    //TODO
    const newConfigObj = JSON.parse(newConfig) as HtmlSection

    console.log(newConfig)
    const newSection = {
      ...htmlPage.sections[sectionIndex],
      sectionConfig: newConfig,
      sectionType: newConfigObj.sectionType
    }
    console.log(newSection)
    const newSectionArray = [...htmlPage.sections]
    console.log(newSectionArray)
    newSectionArray[sectionIndex] = newSection
    console.log(newSectionArray)
    htmlPage = {
      ...htmlPage,
      sections: newSectionArray
    }
    console.log(htmlPage)
    updatePage(htmlPage)
  }

  //Inserts a new HtmlSection at the specified index on the page
  const insertNewSection = (sectionIndex: number, newSection: HtmlSection) => {
    const newSectionClean = {
      ...newSection,
      id: ''
    }
    const newSectionArray = [...htmlPage.sections]
    newSectionArray.splice(sectionIndex, 0, newSectionClean)
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
            section={section} sectionIndex={index} readOnly={readOnly} updateSectionConfig={updateSectionConfig}/>
        </div>
        <div className="col-md-8">
          <HtmlSectionView section={section}/>
        </div>
        <div className="col-md-12 my-2" style={{ backgroundColor: '#eee' }}>
          <button className="btn btn-secondary"
            onClick={() => insertNewSection(index + 1, htmlPage.sections.at(index)!)}>
            <FontAwesomeIcon icon={faPlus}/> Insert section
          </button>
        </div>
      </div>
    })}
  </div>
}

export default HtmlPageView
