import React  from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Study, StudyEnvironment } from 'api/api'
import { Link } from 'react-router-dom'
import { faCogs } from '@fortawesome/free-solid-svg-icons/faCogs'
import { faClipboardCheck } from '@fortawesome/free-solid-svg-icons/faClipboardCheck'
import { faUsers } from '@fortawesome/free-solid-svg-icons/faUsers'
import EnvironmentPublishControl from './EnvironmentPublishControl'

const ENVIRONMENT_ORDER = ['sandbox', 'irb', 'live']

const ENVIRONMENT_ICON_MAP: Record<string, React.ReactNode> = {
  sandbox: <FontAwesomeIcon className="fa-3x ms-2 env-icon text-muted" icon={faCogs}/>,
  irb: <FontAwesomeIcon className="fa-3x ms-2 env-icon text-gray text-muted" icon={faClipboardCheck}/>,
  live: <FontAwesomeIcon className="fa-3x ms-2  env-icon text-gray text-muted" icon={faUsers}/>
}

/** shows the study environments and configuration options */
export default function StudyDashboard({ study }: {study: Study}) {
  const sortedEnvironments = ENVIRONMENT_ORDER
    .map(envName => study.studyEnvironments.find(env => env.environmentName === envName))
    .filter(e => e) as StudyEnvironment[]

  /** copies one environment to another */
  async function publish() {
    alert('not yet implemented')
    // const updatedEnv = await Api.publishFromEnvironment(study.shortname, source, dest)
    // const updatedStudy = _cloneDeep(study)
    // const environmentIndex = updatedStudy.studyEnvironments.findIndex(env => env.environmentName === dest)
    // updatedStudy.studyEnvironments[environmentIndex] = updatedEnv
    // studyContext.updateStudy(updatedStudy)
  }

  return <div className="container">
    <div className="row">
      <div className="col-12 mt-5">
        <h3>{study.name}</h3>
        <div>
          <ul className="list-group">
            {sortedEnvironments.map(studyEnv => {
              return <li key={studyEnv.environmentName} className="list-group-item d-flex m-1">
                <div className="ms-2 ps-2"
                  style={{ borderRight: '1px solid #ccc', minWidth: '200px', maxWidth: '200px' }}>
                  <h5><Link to={`env/${studyEnv.environmentName}`}>{studyEnv.environmentName}</Link></h5>
                  {ENVIRONMENT_ICON_MAP[studyEnv.environmentName]}
                </div>
                <div className="flex-grow-1 ps-3">
                  {studyEnv.studyEnvironmentConfig.initialized &&
                    <EnvironmentSummary studyEnv={studyEnv}/>
                  }
                  {!studyEnv.studyEnvironmentConfig.initialized &&
                    <EnvironmentEmptyMessage studyEnv={studyEnv} />
                  }
                  <hr/>
                  <EnvironmentPublishControl study={study} destEnv={studyEnv} publishFunc={publish}/>
                </div>
              </li>
            })}
          </ul>
        </div>
      </div>
    </div>
  </div>
}

/** shows the basic setup of a study environment */
function EnvironmentSummary({ studyEnv }: {studyEnv: StudyEnvironment}) {
  const config = studyEnv.studyEnvironmentConfig
  return <div>
    <label>Password protected:</label> {config.passwordProtected ? 'Yes' : 'No'}
    { config.passwordProtected && <span className="detail ms-3">{config.password}</span> }
    <br/>
    <label>Accepting enrollment:</label> {config.acceptingEnrollment ? 'Yes' : 'No'}<br/>
    <p>
      <Link to={`env/${studyEnv.environmentName}`}>View / Configure</Link>
    </p>
    <p>
      <Link to={`env/${studyEnv.environmentName}/participants`}>Participants</Link>
    </p>
    <p>
      <Link to={`env/${studyEnv.environmentName}/kits`}>Kits</Link>
    </p>
  </div>
}

/** indicates that an environment is not yet initialized */
function EnvironmentEmptyMessage({ studyEnv }: {studyEnv: StudyEnvironment}) {
  if (studyEnv.environmentName === 'irb') {
    return <div>
      <h6 className="mt-2"><i>Not yet configured</i></h6>  <br/>
      Once you have finished setting up and testing your <Link to={`env/sandbox`}>Sandbox</Link> environment,
      publish it here for your IRB to review it.<br/>
    </div>
  }
  return <div>
    <h6 className="mt-2"><i>Not yet configured</i></h6><br/>
    Once everything is complete, tested,
    and IRB approved in the <Link to={`env/irb`}>IRB</Link> environment,
    publish it here.<br/>
  </div>
}
