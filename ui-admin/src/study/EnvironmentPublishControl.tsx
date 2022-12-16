import { Study, StudyEnvironment } from '../api/api'
import Modal from 'react-bootstrap/Modal'
import React, { useState } from 'react'
import Select from 'react-select'

const ALLOWED_COPY_FLOWS: any = {
  irb: ['sandbox'],
  sandbox: ['irb', 'live'],
  live: ['irb']
}


function EnvironmentPublishControl({ destEnv, study, publishFunc }: {destEnv: StudyEnvironment,
  publishFunc: (source: string, dest: string) => void, study: Study}) {
  const [showModal, setShowModal] = useState(false)
  const destEnvName = destEnv.environmentName

  const initializedEnvironmentNames = getInitializedEnvironmentNames(study)
  const allowedSourceNames = ALLOWED_COPY_FLOWS[destEnvName].filter((envName: string) => {
    return initializedEnvironmentNames.includes(envName)
  })
  const [sourceEnvName, setSourceEnvName] = useState<string>(allowedSourceNames[0])

  function handleOk() {
    publishFunc(sourceEnvName, destEnvName)
    setShowModal(false)
  }
  const opts = allowedSourceNames.map((name: string) => ({
    label: name, value: name
  }))
  const currentVal = { label: sourceEnvName, value: sourceEnvName }
  let envSelector = <></>
  if (allowedSourceNames.length == 1) {
    envSelector = <button className="btn btn-secondary" onClick={() => setShowModal(true)}>
      Copy from {sourceEnvName}
    </button>
  }

  if (allowedSourceNames.length > 1) {
    envSelector = <> Copy from&nbsp;
      <Select options={opts} value={currentVal}
        onChange={(opt: any) => setSourceEnvName(opt?.value)} />
      <button className="btn btn-secondary" onClick={() => setShowModal(true)}>Copy</button>
    </>
  }

  return <div className="d-flex align-items-baseline">
    {envSelector}
    <Modal show={showModal} onHide={() => setShowModal(false)}>
      <Modal.Header closeButton>
        <Modal.Title>Confirm publish</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <div className="text-center mt-4">
          <p>This will copy all content from the {sourceEnvName} environment to the {destEnvName} environment,
            including surveys, consent forms, and home page content.</p>
          { destEnvName === 'LIVE' && <p>
            This is <b>making changes to the live research environment</b>!
            All content should be approved by your IRB prior to performing this action.
          </p> }
        </div>
      </Modal.Body>
      <Modal.Footer>
        <button type="button" className="btn btn-primary"
          onClick={handleOk}>Ok</button>
        <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
      </Modal.Footer>
    </Modal>
  </div>
}

function getInitializedEnvironmentNames(study: Study): string[] {
  return study.studyEnvironments.filter(env => env.studyEnvironmentConfig.initialized)
    .map(env => env.environmentName)
}

export default EnvironmentPublishControl
