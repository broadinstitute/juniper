import {
  AddressValidationResult,
  explainAddressValidationResults,
  isAddressFieldValid,
  MailingAddress
} from '@juniper/ui-core'
import React, { useState } from 'react'
import { doApiLoad } from '../api/api-utils'
import Api from '../api/api'
import { isNil, sortBy } from 'lodash'
import LoadingSpinner from '../util/LoadingSpinner'
import SuggestBetterAddressModal from './SuggestBetterAddressModal'
import { useUser } from '../user/UserProvider'
import { findDifferencesBetweenObjects } from '../util/objectUtils'
import CreatableSelect from 'react-select/creatable'

// Supported country alpha-2 codes; see
// SmartyInternationalAddressValidationService in core
const SUPPORTED_COUNTRIES = [
  'CA',
  'MX',
  'GB',
  'US'
]

/**
 * Editable mailing address component with connection to the backend
 * address validation service.
 */
export default function EditMailingAddress(
  {
    mailingAddress, onMailingAddressFieldChange, setMailingAddress, language
  }: {
    mailingAddress: MailingAddress,
    onMailingAddressFieldChange: (field: keyof MailingAddress, value: string) => void,
    setMailingAddress: (update: MailingAddress) => void,
    language?: string
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

  const calcCountryOptionsForLang = () => {
    // automatically gets internationalized country names based upon the given language
    const names = new Intl.DisplayNames([language || 'en'], { type: 'region' })

    return SUPPORTED_COUNTRIES.map(code => {
      return {
        value: code,
        label: names.of(code)
      }
    })
  }

  const countryOptions = calcCountryOptionsForLang()

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
      setHasChangedSinceValidation([])
    }, { setIsLoading: setIsLoadingValidation })
  }

  const formatClassName = (field: keyof MailingAddress, baseClasses = 'form-control') => {
    if (!addressValidationResults || !isNil(addressValidationResults.suggestedAddress)) {
      return baseClasses
    }

    if (!hasChangedSinceValidation.includes(field)
      && addressValidationResults.valid
      && isNil(addressValidationResults.suggestedAddress)
      && (isNil(addressValidationResults.hasInferredComponents)
        || !addressValidationResults.hasInferredComponents)) {
      return `${baseClasses} is-valid`
    }

    if (!hasChangedSinceValidation.includes(field)
      && !isAddressFieldValid(addressValidationResults, field)) {
      return `${baseClasses} is-invalid`
    }

    return baseClasses
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

  const findCountryLabel = (val: string) => {
    const option = countryOptions.find(option => option.value === val)

    if (option) {
      return option.label
    }

    return val
  }

  const isSupportedCountry = (val: string) => {
    return countryOptions.findIndex(opt => opt.label === val || opt.value === val) >= 0
  }

  return <div className="">
    <div className='row mb-2'>
      <div className="col">
        <input
          className={formatClassName('street1')}
          type="text" value={mailingAddress.street1 || ''} placeholder={'Street 1'}
          onChange={e => onFieldChange('street1', e.target.value)}/>
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
        <CreatableSelect
          styles={{
            control: baseStyles => ({
              ...baseStyles,
              borderColor: 'var(--bs-border-color)' // use same border color as all other components
            })
          }}
          placeholder={'Country'}
          options={sortBy(countryOptions, opt => opt.label)}
          value={{
            value: mailingAddress.country,
            label: findCountryLabel(mailingAddress.country)
          }}
          formatCreateLabel={val => val}
          onChange={val => onFieldChange('country', val ? val.value : '')}
        />
      </div>
    </div>
    {(user.user.superuser) &&
        <LoadingSpinner isLoading={isLoadingValidation}>
          <div
            className={'d-inline-block'}
            title={isSupportedCountry(mailingAddress.country)
              ? 'Validate address'
              : `Validation is not supported for ${mailingAddress.country}`}>
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
