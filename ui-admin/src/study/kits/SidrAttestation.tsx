import React, { useState } from 'react'
import { StudyEnvironmentConfig } from '@juniper/ui-core'

/**
 * The attestations study staff must agree to when ordering research kits through SIDR. This wording mirrors
 * the attestations SIDR records for the research kit tests (RESEARCH_KIT_USE_ONLY and RESEARCH_KIT_SHIPPING_CONSENT),
 * and should be kept in sync with them.
 */
const KIT_USE_ONLY_ATTESTATION =
  'I understand that I am ordering a Research Use Only (RUO) kit which is not intended for use in conjunction ' +
  'with a clinical diagnostic test. The kit is being supplied to a participant to collect a biological ' +
  'specimen for research purposes in accordance with an approved IRB/ethics protocol.'

/** for kits that are mailed to the participant */
export const SIDR_KIT_ATTESTATIONS = [
  KIT_USE_ONLY_ATTESTATION,
  'The participant has consented to receive the sample collection materials at the designated shipping address.'
]

/** for kits that already have a barcode (in-person and manually assigned kits), which SIDR doesn't ship */
export const SIDR_RETURN_ONLY_KIT_ATTESTATIONS = [KIT_USE_ONLY_ATTESTATION]

/** whether kit requests for this study environment go through SIDR, and so require attestation */
export const requiresSidrAttestation = (config?: StudyEnvironmentConfig) => config?.kittingSystem === 'SIDR'

/**
 * Hook for the SIDR attestation checkbox.  AttestationControl renders nothing if the study env doesn't use SIDR,
 * in which case attestationAccepted is undefined and canSubmit is always true.
 *
 * @param returnOnly whether the kit already has a barcode and isn't shipped, which has a shorter attestation
 */
export const useSidrAttestation = (config?: StudyEnvironmentConfig, returnOnly = false) => {
  const required = requiresSidrAttestation(config)
  const [accepted, setAccepted] = useState(false)
  const attestations = returnOnly ? SIDR_RETURN_ONLY_KIT_ATTESTATIONS : SIDR_KIT_ATTESTATIONS

  const AttestationControl = required ? <div className="mt-3">
    <label className="form-label d-flex align-items-start">
      <input type="checkbox" className="mt-1 me-2 flex-shrink-0" checked={accepted}
        aria-label="Order acknowledgements"
        onChange={e => setAccepted(e.target.checked)}/>
      <span>
        By checking this box, the orderer agrees:
        <ul className="mt-1 mb-0">
          {attestations.map(text => <li key={text}>{text}</li>)}
        </ul>
      </span>
    </label>
  </div> : null

  return {
    attestationAccepted: required ? accepted : undefined,
    canSubmit: !required || accepted,
    /** call after submitting, so the next kit has to be attested to again */
    resetAttestation: () => setAccepted(false),
    AttestationControl
  }
}
