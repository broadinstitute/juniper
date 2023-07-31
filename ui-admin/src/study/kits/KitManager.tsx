import React from 'react'
import { Tab, Tabs } from 'react-bootstrap'
import { useNavigate } from 'react-router-dom'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import KitEnrolleeSelection from './KitEnrolleeSelection'
import KitList from './KitList'

/** Top-level kit management screen. */
export default function KitManager({ studyEnvContext, tab }: {
  studyEnvContext: StudyEnvContextT,
  tab: string
}) {
  const { study } = studyEnvContext
  const navigate = useNavigate()

  return <div className="ParticipantList container pt-2">
    <div className="row">
      <div className="col-12">
        <h2 className="h5">{study.name} Sample Kits</h2>
        <Tabs
          activeKey={tab}
          mountOnEnter
          unmountOnExit
          onSelect={tab => {
            navigate(`../${tab}`)
          }}
        >
          <Tab title='Enrollees' eventKey='byEnrollee'>
            <KitEnrolleeSelection studyEnvContext={studyEnvContext}/>
          </Tab>
          <Tab title='Kits' eventKey='byKit'>
            <KitList studyEnvContext={studyEnvContext}/>
          </Tab>
        </Tabs>
      </div>
    </div>
  </div>
}
