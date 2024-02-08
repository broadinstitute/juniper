import {
  AddressValidationResult,
  explainAddressValidationResults,
  isAddressFieldValid,
  MailingAddress
} from '@juniper/ui-core'
import React, { useState } from 'react'
import { doApiLoad } from '../api/api-utils'
import Api from '../api/api'
import { isNil } from 'lodash'
import LoadingSpinner from '../util/LoadingSpinner'
import SuggestBetterAddressModal from './SuggestBetterAddressModal'
import { useUser } from '../user/UserProvider'
import AutocompleteAddressInput from './AutocompleteAddressInput'

/**
 * Editable mailing address component with connection to the backend
 * address validation service.
 */
export default function EditMailingAddress(
  {
    mailingAddress, onMailingAddressFieldChange, setMailingAddress
  }: {
    mailingAddress: MailingAddress,
    onMailingAddressFieldChange: (field: keyof MailingAddress, value: string) => void,
    setMailingAddress: (update: MailingAddress) => void
  }
) {
  const [addressValidationResults, setAddressValidationResults] = useState<AddressValidationResult | undefined>()
  const [isLoadingValidation, setIsLoadingValidation] = useState<boolean>(false)
  const [hasChangedSinceValidation, setHasChangedSinceValidation] = useState<string[]>([])

  const user = useUser()

  const onFieldChange = (field: keyof MailingAddress, value: string) => {
    if (!hasChangedSinceValidation.includes(field)) {
      setHasChangedSinceValidation(val => val.concat([field]))
    }

    onMailingAddressFieldChange(field, value)
  }

  const validateAddress = () => {
    doApiLoad(async () => {
      const results = await Api.validateAddress(mailingAddress)
      setAddressValidationResults(results)
      setHasChangedSinceValidation([])
    }, { setIsLoading: setIsLoadingValidation })
  }

  const formatClassName = (field: keyof MailingAddress) => {
    if (!addressValidationResults) {
      return 'form-control'
    }

    if (!hasChangedSinceValidation.includes(field)
      && addressValidationResults.valid
      && isNil(addressValidationResults.suggestedAddress)
      && (isNil(addressValidationResults.hasInferredComponents)
        || !addressValidationResults.hasInferredComponents)) {
      return 'form-control is-valid'
    }

    if (!hasChangedSinceValidation.includes(field)
      && !isAddressFieldValid(addressValidationResults, field)) {
      return 'form-control is-invalid'
    }

    return 'form-control'
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


  return <div className="">
    <div className='row mb-2'>
      <div className="col">
        {
          user.user.superuser
            ? <AutocompleteAddressInput
              value={mailingAddress.street1 || ''}
              setValue={val => onFieldChange('street1', val)}
              onSelectMailingAddress={
                addr => {
                  setMailingAddress({
                    ...mailingAddress,
                    ...addr
                  })
                  setAddressValidationResults(undefined)
                }
              }
              inputProps={
                {
                  className: formatClassName('street1'),
                  placeholder: 'Street 1'
                }
              }
            />
            : <input
              className={formatClassName('street1')}
              type="text" value={mailingAddress.street1 || ''} placeholder={'Street 1'}
              onChange={e => onFieldChange('street1', e.target.value)}/>
        }

      </div>
    </div>
    <div className='row mb-2'>
      <div className="col">
        <input
          className={formatClassName('street2')}
          type="text" value={mailingAddress.street2 || ''}
          placeholder={'Street 2'}
          onChange={e => onFieldChange('street2', e.target.value)}/>
      </div>
    </div>
    <div className='row mb-2'>
      <div className="col">
        <input
          className={formatClassName('city')}
          type="text" value={mailingAddress.city || ''}
          placeholder={'City'}
          onChange={e => onFieldChange('city', e.target.value)}/>
      </div>
      <div className='col'>
        <input
          className={formatClassName('state')}
          type="text" value={mailingAddress.state || ''} placeholder={'State/Province'}
          onChange={e => onFieldChange('state', e.target.value)}/>
      </div>
    </div>
    <div className='row'>
      <div className="col">
        <input
          className={formatClassName('postalCode')}
          type="text" value={mailingAddress.postalCode || ''} placeholder={'Postal Code'}
          onChange={e => onFieldChange('postalCode', e.target.value)}/>
      </div>
      <div className='col'>
        <input
          className={formatClassName('country')}
          type="text" value={mailingAddress.country || ''} placeholder={'Country'}
          onChange={e => onFieldChange('country', e.target.value)}/>
      </div>
    </div>
    {user.user.superuser && <LoadingSpinner isLoading={isLoadingValidation}>
      <button className="btn btn-link" onClick={validateAddress}>Validate</button>
    </LoadingSpinner>
    }
    {addressValidationResults?.suggestedAddress &&
          <SuggestBetterAddressModal
            inputtedAddress={mailingAddress}
            improvedAddress={addressValidationResults.suggestedAddress}
            hasInferredComponents={addressValidationResults.hasInferredComponents || false}
            accept={() => {
              if (addressValidationResults && addressValidationResults.suggestedAddress) {
                setMailingAddress({
                  ...mailingAddress,
                  ...addressValidationResults.suggestedAddress
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
