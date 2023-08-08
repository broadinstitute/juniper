import React from 'react'
import Select from 'react-select'
import { Portal } from '@juniper/ui-core'
import { getImageUrl } from 'api/api'

/** selects a given study and containing portal */
export default function StudySelector({ portalList, selectedShortcode, setSelectedStudy }:
 {portalList: Portal[], selectedShortcode: string,
     setSelectedStudy: (portalShortcode: string, studyShortcode: string) => void}) {
  /** the same study may appear in multiple portals, so we need to track the associated shortcode */
  const options = portalList
    .flatMap(portal => portal.portalStudies.map(ps =>
      ({ label: ps.study.name, value: ps.study.shortcode, portalCode: portal.shortcode })))
  const selectedOpt = options
    .find(opt => opt.value === selectedShortcode)
  return <Select options={options}
    value={selectedOpt}
    onChange={opt => {
      if (opt) {
        setSelectedStudy(opt.portalCode, opt.value)
      }
    }}
    formatOptionLabel={opt => (<div>
      <img
        src={getImageUrl(opt.portalCode, 'favicon.ico', 1)}
        className="me-2" style={{ maxHeight: '1.5em' }}/>
      {opt.label}
    </div>)}
  />
}
