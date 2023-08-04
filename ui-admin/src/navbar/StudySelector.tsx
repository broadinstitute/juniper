import React from 'react'
import Select from 'react-select'
import {Portal, Study} from "@juniper/ui-core";
import {getImageUrl} from "api/api";

export default function StudySelector({portalList, selectedShortcode}:
 {portalList: Portal[], selectedShortcode: string, setSelectedShortcode: (shortcode: string) => void}) {

    const options = portalList
        .flatMap(portal => portal.portalStudies.map(ps =>
            ({label: ps.study.name, value: ps.study.shortcode, portalCode: portal.shortcode})))
    const selectedOpt = options
        .find(opt => opt.value = selectedShortcode)
    return <Select options={options} value={selectedOpt}
                   formatOptionLabel={opt => (<div>
                       <img src={getImageUrl(opt.portalCode, 'favicon.ico', 1)}/>
                       {opt.label}
                   </div>)}/>
}