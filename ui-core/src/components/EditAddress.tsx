import { AddressValidationResult, MailingAddress } from 'src/types/address'
import classNames from 'classnames'
import { isNil, sortBy } from 'lodash'
import React, { useEffect, useState } from 'react'
import { getAllCountries } from '../i18nUtils'
import { isAddressFieldValid } from '../addressUtils'
import CreatableSelect from 'react-select/creatable'

/**
 * Editable address component. Displays errors if a validation result is provided,
 * but does not have any modals for, e.g., suggest address or the full editing workflow.
 */
export function EditAddress(
  {
    mailingAddress,
    setMailingAddress,
    validationResult,
    showLabels = true,
    language = 'en',
    i18n
  }: {
    mailingAddress: MailingAddress,
    setMailingAddress: (updated: React.SetStateAction<MailingAddress>) => void,
    validationResult?: AddressValidationResult,
    showLabels: boolean,
    language: string,
    i18n?: (key: string) => string
  }
) {
  const [hasChangedSinceValidation, setHasChangedSinceValidation] = useState<string[]>([])

  const onFieldChange = (field: keyof MailingAddress, value: string) => {
    if (!hasChangedSinceValidation.includes(field)) {
      setHasChangedSinceValidation(val => val.concat([field]))
    }

    setMailingAddress(old => {
      return {
        ...old,
        [field]: value
      }
    })
  }

  useEffect(() => {
    setHasChangedSinceValidation([])
  }, [validationResult])

  const countryOptions = getAllCountries(language).map(
    country => {
      return {
        value: country.code,
        label: country.name
      }
    }
  )

  const findCountryLabel = (val: string) => {
    const option = countryOptions.find(option => option.value === val)

    if (option) {
      return option.label
    }

    return val
  }

  // checks if a field should be in an error state; if true,
  // should be green, if false, red, and if undefined, unhighlighted
  const isValid = (field: keyof MailingAddress): boolean | undefined => {
    if (!validationResult || !isNil(validationResult.suggestedAddress)) {
      return undefined
    }

    if (!hasChangedSinceValidation.includes(field)
      && validationResult.valid
      && isNil(validationResult.suggestedAddress)
      && (isNil(validationResult.hasInferredComponents)
        || !validationResult.hasInferredComponents)) {
      return true
    }

    if (!hasChangedSinceValidation.includes(field)
      && !isAddressFieldValid(validationResult, field)) {
      return false
    }

    return undefined
  }

  const labels = i18n ? {
    'street1': i18n('street1'),
    'street2': i18n('street2'),
    'city': i18n('city'),
    'country': i18n('country'),
    'postalCode': i18n('postalCode'),
    'state': i18n('state')
  } : {
    'street1': 'Street 1',
    'street2': 'Street 2',
    'city': 'City',
    'country': 'Country',
    'postalCode': 'Postal Code',
    'state': 'State'
  }

  return <>
    <div className='row mb-2'>
      <div className="col">
        <label
          htmlFor={'street1'}
          className={'fs-6 fw-bold'}
          hidden={!showLabels}
        >
          {labels['street1']}
        </label>
        <input
          className={classNames(
            'form-control', {
              'is-valid': isValid('street1') === true,
              'is-invalid': isValid('street1') === false
            })}
          type="text"
          id="street1"
          value={mailingAddress.street1 || ''}
          placeholder={labels['street1']}
          onChange={e => onFieldChange('street1', e.target.value)}/>
      </div>
    </div>
    <div className='row mb-2'>
      <label
        htmlFor={'street2'}
        className={'fs-6 fw-bold'}
        hidden={!showLabels}
      >
        {labels['street2']}
      </label>
      <div className="col">
        <input
          className={classNames(
            'form-control', {
              'is-valid': isValid('street2') === true,
              'is-invalid': isValid('street2') === false
            })}
          type="text" value={mailingAddress.street2 || ''}
          id="street2"
          placeholder={labels['street2']}
          onChange={e => onFieldChange('street2', e.target.value)}/>
      </div>
    </div>
    <div className='row mb-2'>
      <div className="col">
        <label
          htmlFor={'city'}
          className={'fs-6 fw-bold'}
          hidden={!showLabels}
        >
          {labels['city']}
        </label>
        <input
          className={classNames(
            'form-control', {
              'is-valid': isValid('city') === true,
              'is-invalid': isValid('city') === false
            })}
          type="text" value={mailingAddress.city || ''}
          id="city"
          placeholder={labels['city']}
          onChange={e => onFieldChange('city', e.target.value)}/>
      </div>
      <div className='col'>
        <label
          htmlFor={'state'}
          className={'fs-6 fw-bold'}
          hidden={!showLabels}
        >
          {labels['state']}
        </label>
        <input
          className={classNames(
            'form-control', {
              'is-valid': isValid('state') === true,
              'is-invalid': isValid('state') === false
            })}
          type="text" value={mailingAddress.state || ''}
          id="state"
          placeholder={labels['state']}
          onChange={e => onFieldChange('state', e.target.value)}/>
      </div>
    </div>
    <div className='row'>
      <div className="col">
        <label
          htmlFor={'postalCode'}
          className={'fs-6 fw-bold'}
          hidden={!showLabels}
        >
          {labels['postalCode']}
        </label>
        <input
          className={classNames(
            'form-control', {
              'is-valid': isValid('postalCode') === true,
              'is-invalid': isValid('postalCode') === false
            })}
          type="text" value={mailingAddress.postalCode || ''}
          id="postalCode"
          placeholder={labels['postalCode']}
          onChange={e => onFieldChange('postalCode', e.target.value)}/>
      </div>
      <div className='col'>
        <label
          htmlFor={'country'}
          className={'fs-6 fw-bold'}
          hidden={!showLabels}
        >
          {labels['country']}
        </label>
        <CreatableSelect
          styles={{
            control: baseStyles => ({
              ...baseStyles,
              borderColor: 'var(--bs-border-color)' // use same border color as all other components
            })
          }}
          placeholder={labels['country']}
          id="country"
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
  </>
}
