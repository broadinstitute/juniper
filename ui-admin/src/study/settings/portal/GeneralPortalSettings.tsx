import React from 'react'
import {
  PortalEnvironment,
  PortalEnvironmentConfig,
  PortalStudy
} from '@juniper/ui-core'
import InfoPopup from 'components/forms/InfoPopup'
import Select from 'react-select'
import useReactSingleSelect from 'util/react-select-utils'
import { LoadedPortalContextT } from 'portal/PortalProvider'

export const GeneralPortalSettings = (
  {
    portalContext,
    portalEnv,
    config,
    updateConfig
  } : {
    portalContext: LoadedPortalContextT,
    portalEnv: PortalEnvironment,
    config: PortalEnvironmentConfig,
    updateConfig: (key: keyof PortalEnvironmentConfig, value: unknown) => void,
  }
) => {
  const portal = portalContext.portal


  const {
    onChange: primaryStudyOnChange, options: primaryStudyOptions,
    selectedOption: selectedPrimaryStudyOption, selectInputId: selectPrimaryStudyInputId
  } =
    useReactSingleSelect(
      portal.portalStudies,
      (portalStudy: PortalStudy) =>
        ({ label: portalStudy.study.name, value: portalStudy }),
      (opt: PortalStudy | undefined) => updateConfig('primaryStudy', opt?.study.shortcode),
      portal.portalStudies.find(ps => ps.study.shortcode === config.primaryStudy)
    )


  return <div>
    <div>
      <label className="form-label">
        Email source address
        <input type="text" className="form-control" value={config.emailSourceAddress ?? ''}
          onChange={e => updateConfig('emailSourceAddress', e.target.value)}/>
      </label>
    </div>

    {portal.portalStudies.length > 1 && <div className="mb-3">
      <label className="form-label" htmlFor={selectPrimaryStudyInputId}>
            Primary study</label> <InfoPopup content={'The study that portal registrants will be taken to by default'}/>
      <Select options={primaryStudyOptions} className="col-md-3"
        value={selectedPrimaryStudyOption} inputId={selectPrimaryStudyInputId}
        isDisabled={portalEnv.environmentName !== 'sandbox'} aria-label={'Select a study'}
        onChange={primaryStudyOnChange}/>
    </div>}
  </div>
}
