import React, { useState } from 'react'
import Api from 'api/api'
import {
  AddressValidationResult,
  EditAddress,
  isSameAddress,
  javaLocalDateToJsDate,
  jsDateToJavaLocalDate,
  MailingAddress,
  Profile,
  SuggestBetterAddressModal,
  useI18n
} from '@juniper/ui-core'
import ThemedModal from '../components/ThemedModal'
import Modal from 'react-bootstrap/Modal'
import { isNil } from 'lodash'


// skeleton for all profile edit modals
function ProfileRowEditModal(
  {
    title, children, onSave, onDismiss, animated
  }: {
    title: string, children: React.ReactNode, onSave: () => void, onDismiss: () => void, animated?: boolean
  }
) {
  const { i18n } = useI18n()
  return <ThemedModal show={true} onHide={onDismiss} size={'lg'} animation={animated}> <Modal.Header>
    <Modal.Title>
      <h2 className="fw-bold pb-0 mb-0">{title}</h2>
    </Modal.Title>
  </Modal.Header>
  <Modal.Body>
    {children}
  </Modal.Body>
  <Modal.Footer>
    <div className={'d-flex w-100'}>
      <button className={'btn btn-primary m-2'} onClick={onSave}>{i18n('save')}</button>
      <button className={'btn btn-outline-secondary m-2'} onClick={onDismiss}>{i18n('cancel')}</button>
    </div>
  </Modal.Footer>
  </ThemedModal>
}

// common props for all profile edit modals
type EditModalProps = {
  profile: Profile,
  dismissModal: () => void,
  save: (p: Profile) => void

}

// internal profile editing methods
const useProfileEditMethods = (props: EditModalProps) => {
  const {
    profile,
    dismissModal,
    save
  } = props

  const [editedProfile, setEditedProfile] = useState<Profile>(profile)

  const onDismiss = () => {
    dismissModal()
    // reset profile back to original state so that
    // you don't close a modal then open another
    // and accidentally commit changes from both
    setEditedProfile(profile)
  }

  const onFieldChange = (field: keyof Profile, value: string | boolean | MailingAddress) => {
    setEditedProfile(oldProfile => {
      return {
        ...oldProfile,
        [field]: value
      }
    })
  }

  const onDateFieldChange = (field: keyof Profile, date: Date | null) => {
    const asJavaLocalDate: number[] | undefined = date ? jsDateToJavaLocalDate(date) : undefined

    setEditedProfile((oldVal: Profile) => {
      return {
        ...oldVal,
        [field]: asJavaLocalDate
      }
    })
  }

  const onSave = (profile?: Profile) => {
    if (profile) {
      save(profile)
    } else {
      save(editedProfile)
    }
    dismissModal()
  }

  return {
    onDismiss,
    onSave,
    onDateFieldChange,
    onFieldChange,
    editedProfile
  }
}

/**
 * Modal for editing the givenName and familyName properties on a profile.
 */
export function EditNameModal(props: EditModalProps) {
  const {
    onDismiss,
    onSave,
    onFieldChange,
    editedProfile
  } = useProfileEditMethods(props)

  const { i18n } = useI18n()

  return <ProfileRowEditModal
    title={i18n('editName')}
    onSave={() => onSave()}
    onDismiss={onDismiss}>
    <div className={'d-flex w-100'}>
      <div className={'w-50 p-1'}>
        <label htmlFor={'givenName'} className={'fs-6 fw-bold'}>
          {i18n('givenName')}
        </label>
        <input
          className={'form-control'}
          id={'givenName'}
          value={editedProfile.givenName}
          onChange={e => onFieldChange('givenName', e.target.value)}
          placeholder={i18n('givenName')}/>
      </div>
      <div className={'w-50 p-1'}>
        <label htmlFor={'familyName'} className={'fs-6 fw-bold'}>
          {i18n('familyName')}
        </label>
        <input
          className={'form-control'}
          id={'familyName'}
          value={editedProfile.familyName}
          onChange={e => onFieldChange('familyName', e.target.value)}
          placeholder={i18n('familyName')}/>
      </div>
    </div>
  </ProfileRowEditModal>
}

/**
 * Modal for editing the birthDate property on a profile.
 */
export function EditBirthDateModal(props: EditModalProps) {
  const {
    onDismiss,
    onSave,
    onDateFieldChange,
    editedProfile
  } = useProfileEditMethods(props)

  const { i18n } = useI18n()

  return <ProfileRowEditModal
    title={i18n('editBirthDate')}
    onSave={() => onSave()}
    onDismiss={onDismiss}>

    <label htmlFor={'birthDate'} className={'fs-6 fw-bold'}>
      {i18n('birthDate')}
    </label>
    <input className="form-control" type="date" id='birthDate'
      defaultValue={javaLocalDateToJsDate(editedProfile.birthDate)?.toISOString().split('T')[0] || ''}
      placeholder={i18n('birthDate')} max={'9999-12-31'} aria-label={i18n('birthDate')}
      onChange={e => onDateFieldChange('birthDate', e.target.valueAsDate)}/>

  </ProfileRowEditModal>
}

/**
 * Modal for editing the phoneNumber property on a profile.
 */
export function EditPhoneNumber(props: EditModalProps) {
  const {
    onDismiss,
    onSave,
    onFieldChange,
    editedProfile
  } = useProfileEditMethods(props)

  const { i18n } = useI18n()

  return <ProfileRowEditModal
    title={i18n('editPhoneNumber')}
    onSave={() => onSave()}
    onDismiss={onDismiss}>
    <div>
      <label htmlFor={'phoneNumber'} className={'fs-6 fw-bold'}>
        {i18n('phoneNumber')}
      </label>
      <input
        className={'form-control'}
        type={'tel'} // on mobile, brings up numpad instead of keyboard
        id={'phoneNumber'}
        value={editedProfile.phoneNumber}
        onChange={e => onFieldChange('phoneNumber', e.target.value)}
        placeholder={i18n('phoneNumber')}/>
    </div>
  </ProfileRowEditModal>
}

/**
 * Modal for editing the contactEmail property on a profile.
 */
export function EditContactEmail(props: EditModalProps) {
  const {
    onDismiss,
    onSave,
    onFieldChange,
    editedProfile
  } = useProfileEditMethods(props)

  const { i18n } = useI18n()

  return <ProfileRowEditModal
    title={i18n('editContactEmail')}
    onSave={() => onSave()}
    onDismiss={onDismiss}>
    <div>
      <p className="fst-italic">
        {i18n('editProfileContactEmailWarning')}
      </p>
      <label htmlFor={'contactEmail'} className={'fs-6 fw-bold'}>
        {i18n('contactEmail')}
      </label>
      <input
        className={'form-control'}
        id={'contactEmail'}
        value={editedProfile.contactEmail}
        onChange={e => onFieldChange('contactEmail', e.target.value)}
        placeholder={i18n('contactEmail')}/>
    </div>
  </ProfileRowEditModal>
}

/**
 * Modal for editing the doNotEmail and doNotSolicit properties on a profile.
 */
export function EditCommunicationPreferences(props: EditModalProps) {
  const {
    onDismiss,
    onSave,
    onFieldChange,
    editedProfile
  } = useProfileEditMethods(props)

  const { i18n } = useI18n()

  return <ProfileRowEditModal
    title={i18n('editCommunicationPreferences')}
    onSave={() => onSave()}
    onDismiss={onDismiss}>
    <div className='row mt-2'>
      <div className="col-auto">
        <div className="form-check">
          <input className="form-check-input" type="checkbox"
            checked={editedProfile.doNotEmail} id="doNotEmailCheckbox"
            aria-label={i18n('doNotContact')}
            onChange={e => onFieldChange('doNotEmail', e.target.checked)}/>
          <label className="form-check-label" htmlFor="doNotEmailCheckbox">
            {i18n('doNotContact')}
          </label>
        </div>
      </div>
      <div className='col-auto'>
        <div className="form-check">
          <input className="form-check-input" type="checkbox"
            checked={editedProfile.doNotEmailSolicit}
            id="doNotSolicitCheckbox" aria-label={i18n('doNotSolicit')}
            onChange={e => onFieldChange('doNotEmailSolicit', e.target.checked)}/>
          <label className="form-check-label" htmlFor="doNotSolicitCheckbox">
            {i18n('doNotSolicit')}
          </label>
        </div>
      </div>
    </div>
  </ProfileRowEditModal>
}

/**
 * Modal for editing the mailing address properties on a profile.
 */
export function EditMailingAddressModal(props: EditModalProps) {
  const {
    onDismiss,
    onSave,
    editedProfile
  } = useProfileEditMethods(props)


  const [mailingAddress, setMailingAddress] = useState<MailingAddress>(
    editedProfile.mailingAddress || {
      street1: '',
      street2: '',
      city: '',
      state: '',
      postalCode: '',
      country: ''
    }
  )

  const [validationResults, setValidationResults] = useState<AddressValidationResult>()

  const [animateModal, setAnimateModal] = useState<boolean>(true)

  const shouldShowSuggestedAddress = (results: AddressValidationResult) => {
    if (!results) {
      return false
    }

    return results.valid &&
      (!isNil(results.suggestedAddress) && !isSameAddress(results.suggestedAddress, mailingAddress))
  }

  const buildUpdatedProfile = (addr: MailingAddress) => {
    return {
      ...editedProfile,
      mailingAddress: {
        ...editedProfile.mailingAddress, // grab id, createdAt, etc.
        ...{ // clear out any old values
          street1: '',
          street2: '',
          city: '',
          state: '',
          postalCode: '',
          country: ''
        },
        ...addr
      }
    }
  }

  const [isLoadingValidation, setIsLoadingValidation] = useState<boolean>(false)

  const validateAndSave = async () => {
    setIsLoadingValidation(true)
    try {
      const newValidationResult = await Api.validateAddress(mailingAddress)

      setValidationResults(newValidationResult)
      setAnimateModal(false)

      if (newValidationResult?.valid && !shouldShowSuggestedAddress(newValidationResult)) {
        onSave(buildUpdatedProfile(mailingAddress))
      }
    } finally {
      setIsLoadingValidation(false)
    }
  }

  const tryValidateAndSave = async () => {
    // only validate US addresses
    if (mailingAddress.country === 'US') {
      await validateAndSave()
    } else {
      // if not US, just save whatever they give us
      onSave(buildUpdatedProfile(mailingAddress))
    }
  }

  if (validationResults && shouldShowSuggestedAddress(validationResults) && validationResults?.suggestedAddress) {
    return <SuggestBetterAddressModal
      inputtedAddress={mailingAddress}
      improvedAddress={validationResults?.suggestedAddress}
      accept={() => {
        if (!validationResults?.suggestedAddress) {
          return
        }
        onSave(buildUpdatedProfile(validationResults.suggestedAddress))
      }}
      reject={() => {
        onSave(buildUpdatedProfile(mailingAddress))
      }}
      goBack={() => {
        setValidationResults(undefined)
      }}
      animated={false}
      onDismiss={onDismiss}
      ModalComponent={ThemedModal}
    />
  }

  const { i18n } = useI18n()

  return <ProfileRowEditModal
    title={i18n('editMailingAddress')}
    onSave={() => tryValidateAndSave()}
    animated={animateModal}
    onDismiss={onDismiss}>
    {
      isLoadingValidation
        ? <p>Loading...</p>
        : <EditAddress
          mailingAddress={mailingAddress}
          setMailingAddress={setMailingAddress}
          showLabels={true}
          validationResult={validationResults}
        />
    }
  </ProfileRowEditModal>
}
