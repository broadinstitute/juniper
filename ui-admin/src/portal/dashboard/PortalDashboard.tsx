import React from 'react'

import { Portal } from 'api/api'
import { renderPageHeader } from 'util/pageUtils'

import { StudyWidget } from './widgets/StudyWidget'
import { WebsiteWidget } from './widgets/WebsiteWidget'
import { TutorialsWidget } from './widgets/TutorialsWidget'
import { MailingListWidget } from './widgets/MailingListWidget'
import { StudyTeamWidget } from './widgets/StudyTeamWidget'
import { StudyTrendsWidget } from './widgets/StudyTrendsWidget'
import classNames from 'classnames'
import ErrorBoundary from 'util/ErrorBoundary'

export default function PortalDashboard({ portal }: {portal: Portal}) {
  return <div className="px-4 container-fluid mb-5">
    {renderPageHeader(`${portal.name} Home`)}
    <div className='row'>
      <div className="col col-7">
        <div className='mb-3'><ErrorBoundary><StudyWidget portal={portal}/></ErrorBoundary></div>
        <div className='mb-3'><ErrorBoundary><WebsiteWidget portal={portal}/></ErrorBoundary></div>
      </div>
      <div className="col col-5">
        <div className={'d-flex flex-wrap'}>
          {portal.portalStudies.map((study, i) =>
            <div key={study.study.shortcode}
              className={classNames('container w-50 ps-0 mb-3')} style={{ paddingRight: i % 2 === 0 ? '1rem' : '0' }}>
              <ErrorBoundary><StudyTrendsWidget portal={portal} study={study.study}/></ErrorBoundary>
            </div>
          )}
        </div>
        <div className='mb-3'><ErrorBoundary><MailingListWidget portal={portal}/></ErrorBoundary></div>
        <div className='mb-3'><ErrorBoundary><StudyTeamWidget portal={portal}/></ErrorBoundary></div>
        <div className='mb-3'><ErrorBoundary><TutorialsWidget/></ErrorBoundary></div>
      </div>
    </div>
  </div>
}
