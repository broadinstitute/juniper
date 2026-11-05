import React, { useState } from 'react'
import { StudyEnvironmentConfig } from '@juniper/ui-core'

/**
 * The attestations study staff must agree to when ordering research kits through SIDR. This wording mirrors
 * the attestations SIDR records for the research kit test (RESEARCH_KIT_USE_ONLY and RESEARCH_KIT_SHIPPING_CONSENT),
 * and should be kept in sync with them.
 */
export const SIDR_KIT_ATTESTATIONS = [
  'I understand that I am ordering a Research Use Only (RUO) kit which is not intended for use in conjunction ' +
    'with a clinical diagnostic test. The kit is being supplied to a participant to collect a biological ' +
    'specimen for research purposes in accordance with an approved IRB/ethics protocol.',
  'The participant has consented to receive the sample collection materials at the designated shipping address.'
]

/** whether kit requests for this study environment go through SIDR, and so require attestation */
export const requiresSidrAttestation = (config?: StudyEnvironmentConfig) => config?.kittingSystem === 'SIDR'

/**
 * Hook for the SIDR attestation checkbox.  AttestationControl renders nothing if the study env doesn't use SIDR,
 * in which case attestationAccepted is undefined and canSubmit is always true.
 */
export const useSidrAttestation = (config?: StudyEnvironmentConfig) => {
  const required = requiresSidrAttestation(config)
  const [accepted, setAccepted] = useState(false)

  const AttestationControl = required ? <div className="mt-3">
    <label className="form-label d-flex align-items-start">
      <input type="checkbox" className="mt-1 me-2 flex-shrink-0" checked={accepted}
        aria-label="Order acknowledgements"
        onChange={e => setAccepted(e.target.checked)}/>
      <span>
        By checking this box, the orderer agrees:
        <ul className="mt-1 mb-0">
          {SIDR_KIT_ATTESTATIONS.map(text => <li key={text}>{text}</li>)}
        </ul>
      </span>
    </label>
  </div> : null

  return {
    attestationAccepted: required ? accepted : undefined,
    canSubmit: !required || accepted,
    AttestationControl
  }
}
