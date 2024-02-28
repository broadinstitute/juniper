import { AddressValidationResult, explainAddressValidationResults, MailingAddress } from '@juniper/ui-core'
import React, { useState } from 'react'
import { doApiLoad } from '../api/api-utils'
import Api from '../api/api'
import LoadingSpinner from '../util/LoadingSpinner'
import SuggestBetterAddressModal from './SuggestBetterAddressModal'
import { useUser } from '../user/UserProvider'
import { findDifferencesBetweenObjects } from '../../../ui-core/src/objectUtils'
import EditAddress from '@juniper/ui-core/build/components/EditAddress'

// Supported country alpha-2 codes; see
// SmartyInternationalAddressValidationService in core
const SUPPORTED_COUNTRIES = [
  'US',
  'CA',
  'GB',
  'MX',
  'AU',
  'TR',
  'ES',
  'PL',
  'DE',
  'FR',
  'IT',
  'CZ',
  'BR',
  'SE',
  'CH'
]

/**
 * Editable mailing address component with connection to the backend
 * address validation service.
 */
export default function EditMailingAddress(
  {
    mailingAddress, setMailingAddress
  }: {
    mailingAddress: MailingAddress,
    setMailingAddress: (update: React.SetStateAction<MailingAddress>) => void,
  }
) {
  const [addressValidationResults, setAddressValidationResults] = useState<AddressValidationResult | undefined>()
  const [isLoadingValidation, setIsLoadingValidation] = useState<boolean>(false)

  const user = useUser()

  const validateAddress = () => {
    doApiLoad(async () => {
      const results = await Api.validateAddress(mailingAddress)
      if (results.suggestedAddress
        && findDifferencesBetweenObjects(mailingAddress, results.suggestedAddress)
          .filter(val => !['id', 'createdAt', 'lastUpdatedAt'].includes(val.fieldName))
          .length === 0) {
        results.suggestedAddress = undefined
      }

      setAddressValidationResults(results)
    }, { setIsLoading: setIsLoadingValidation })
  }

  const clearSuggestedAddress = () => {
    setAddressValidationResults(val => {
      return {
        ...(val || {}), // technically it could be undefined but should be impossible when this is called
        valid: true,
        suggestedAddress: undefined
      }
    })
  }

  const isSupportedCountry = (val: string) => {
    return SUPPORTED_COUNTRIES.includes(val)
  }

  return <div className="">
    <EditAddress
      mailingAddress={mailingAddress}
      setMailingAddress={setMailingAddress}
      showLabels={false}
      validationResult={addressValidationResults}
      language={'en'}
    />
    {(user.user.superuser) &&
        <LoadingSpinner isLoading={isLoadingValidation}>
          <div
            className={'d-inline-block'}
            title={isSupportedCountry(mailingAddress.country)
              ? 'Validate address'
              : `Validation is not supported for this country`}>
            <button
              className="btn btn-link" disabled={!isSupportedCountry(mailingAddress.country)}
              onClick={validateAddress}>
                    Validate
            </button>
          </div>

        </LoadingSpinner>
    }
    {addressValidationResults?.suggestedAddress &&
        <SuggestBetterAddressModal
          inputtedAddress={mailingAddress}
          improvedAddress={addressValidationResults.suggestedAddress}
          hasInferredComponents={addressValidationResults.hasInferredComponents || false}
          accept={() => {
            if (addressValidationResults && addressValidationResults.suggestedAddress) {
              const suggested = addressValidationResults.suggestedAddress
              setMailingAddress({
                ...mailingAddress, // keep same id/createdAt, etc.
                ...{
                  street1: suggested.street1,
                  street2: suggested.street2,
                  country: suggested.country,
                  city: suggested.city,
                  postalCode: suggested.postalCode,
                  state: suggested.state
                } // only update relevant fields (not, e.g., id or createdAt)
              })
            }
            // clear the results since we saved the new address
            setAddressValidationResults(undefined)
          }}
          deny={() => {
            clearSuggestedAddress()
          }}
          onDismiss={() => {
            clearSuggestedAddress()
          }}
        />}
    {!addressValidationResults?.valid && explainAddressValidationResults(addressValidationResults)
      .map((explanation, idx) =>
        <p key={idx} className="text-danger-emphasis">{explanation}</p>
      )}
  </div>
}
