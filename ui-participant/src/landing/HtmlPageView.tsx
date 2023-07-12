import _ from 'lodash'
import React from 'react'

import { HtmlPage, HtmlSection } from 'api/api'
import { DocumentTitle } from 'util/DocumentTitle'
import { HtmlSectionView } from "@juniper/ui-core";


/** renders a configured HtmlPage */
export default function HtmlPageView({ page }: { page: HtmlPage }) {
  return <>
    <DocumentTitle title={page.title} />
    {
      _.map(page.sections, (section: HtmlSection) => <HtmlSectionView section={section} key={section.id}/>)
    }
  </>
}
