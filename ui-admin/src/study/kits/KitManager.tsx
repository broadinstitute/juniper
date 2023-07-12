import React, { useState } from 'react'
import { Tab, Tabs } from 'react-bootstrap'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import KitEnrolleeSelection from './KitEnrolleeSelection'
import KitList from './KitList'


/** Top-level kit management screen. */
export default function KitManager({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const { study } = studyEnvContext
  const [activeTab, setActiveTab] = useState<string | null>('enrollees')

  return <div className="ParticipantList container pt-2">
    <div className="row">
      <div className="col-12">
        <h2 className="h5">{study.name} Sample Kits</h2>
        <Tabs
          activeKey={activeTab ?? undefined}
          mountOnEnter
          unmountOnExit
          onSelect={setActiveTab}
        >
          <Tab title='Enrollees' eventKey='enrollees'>
            <KitEnrolleeSelection studyEnvContext={studyEnvContext}/>
          </Tab>
          <Tab title='Kits' eventKey='kits'>
            <KitList studyEnvContext={studyEnvContext}/>
          </Tab>
        </Tabs>
      </div>
    </div>
  </div>
}
