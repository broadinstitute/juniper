import React from 'react'
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faPlus} from "@fortawesome/free-solid-svg-icons";
import {HtmlPage, HtmlSectionView} from "@juniper/ui-core";
import Select from 'react-select'
import {HtmlSection, NavbarItemInternal} from "@juniper/ui-core/build/types/landingPageConfig";

const SECTION_TYPES = [
  {label: 'FAQ', value: 'FAQ'},
  {label: 'HERO_CENTERED', value: 'HERO_CENTERED'},
  {label: 'HERO_WITH_IMAGE', value: 'HERO_WITH_IMAGE'},
  {label: 'SOCIAL_MEDIA', value: 'SOCIAL_MEDIA'},
  {label: 'STEP_OVERVIEW', value: 'STEP_OVERVIEW'},
  {label: 'PHOTO_BLURB_GRID', value: 'PHOTO_BLURB_GRID'},
  {label: 'PARTICIPATION_DETAIL', value: 'PARTICIPATION_DETAIL'},
  {label: 'RAW_HTML', value: 'RAW_HTML'},
  {label: 'LINK_SECTIONS_FOOTER', value: 'LINK_SECTIONS_FOOTER'},
  {label: 'BANNER_IMAGE', value: 'BANNER_IMAGE'}
]

type HtmlPageViewProps = {
  htmlPage: HtmlPage,
  updatePage: (page: HtmlPage) => void
}


const HtmlPageView = ({ htmlPage, updatePage }: HtmlPageViewProps) => {

  const updateSection = (section: HtmlSection, index: number) => {
    htmlPage.sections[index] = section
    updatePage(htmlPage)
  }

  const updateSectionConfig = (sectionIndex: number, newConfig: string) => {
    try {
      JSON.parse(newConfig)
    } catch (e) {
      return
    }
    const newSection = {
      ...htmlPage.sections[sectionIndex],
      sectionConfig: newConfig
    }
    const newSectionArray = [...htmlPage.sections]
    newSectionArray[sectionIndex] = newSection
    htmlPage = {
      ...htmlPage,
      sections: newSectionArray
    }
    updatePage(htmlPage)
  }

  return <div>
    {htmlPage.sections.map((section, index) => {
      const textValue = JSON.stringify(JSON.parse(section?.sectionConfig ?? '{}'), null, 2)
      const sectionTypeOpt = SECTION_TYPES.find(sectionType => sectionType.value === section.sectionType)
      return <div key={index} className="row">

        <div className="col-md-4 p-2">
          <div>
            <Select options={SECTION_TYPES} value={sectionTypeOpt}/>
          </div>
          <textarea value={textValue} style={{height: 'calc(100% - 2em)', width: '100%'}}
            onChange={e => updateSectionConfig(index, e.target.value)}/>
        </div>
        <div className="col-md-8">
          <HtmlSectionView section={section}/>
        </div>
        <div className="col-md-12 my-2" style={{backgroundColor: '#eee'}}>
          <button className="btn btn-secondary" onClick={() => alert('not yet implemented')}>
            <FontAwesomeIcon icon={faPlus}/> Insert section
          </button>
        </div>
      </div>
    })}
  </div>
}

export default HtmlPageView
