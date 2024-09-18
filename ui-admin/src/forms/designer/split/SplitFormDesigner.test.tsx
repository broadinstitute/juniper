import React from 'react'

import { SplitFormDesigner } from './SplitFormDesigner'
import { screen, act } from '@testing-library/react'
import { renderWithRouter } from '@juniper/ui-core'

jest.spyOn(window, 'scrollTo').mockImplementation(() => { })

describe('SplitFormDesigner', () => {
  it('should render SplitFormDesigner for an empty form', () => {
    renderWithRouter(
      <SplitFormDesigner
        content={{ 'title': 'Test empty form', 'pages': [{ 'elements': [] }] }}
        onChange={() => jest.fn()}
        currentLanguage={{ id: '0', languageCode: 'en', languageName: 'English' }}
        supportedLanguages={[{ id: '0', languageCode: 'en', languageName: 'English' }]}
      />)
    expect(screen.getByLabelText('Insert a new question')).toBeInTheDocument()
    expect(screen.getByLabelText('Insert a new panel')).toBeInTheDocument()
    expect(screen.getAllByLabelText('Go to previous page')[0]).toBeInTheDocument()
    expect(screen.getByText('Scroll to top')).toBeInTheDocument()
    expect(screen.getAllByLabelText('Go to next page')[0]).toBeInTheDocument()
  })

  it('should handle page change', () => {
    renderWithRouter(
      <SplitFormDesigner
        content={
          {
            'title': 'Test two page form',
            'pages': [{
              'elements': [
                { 'type': 'text', 'name': 'q1', 'title': 'question1' }
              ]
            }, {
              'elements': [
                { 'type': 'text', 'name': 'q2', 'title': 'question2' }
              ]
            }]
          }}
        onChange={() => jest.fn()}
        currentLanguage={{ id: '0', languageCode: 'en', languageName: 'English' }}
        supportedLanguages={[{ id: '0', languageCode: 'en', languageName: 'English' }]}
      />)

    //Confirm initial page view is correct
    expect(screen.getAllByText('question1')).toHaveLength(2)
    expect(screen.queryAllByText('question2')).toHaveLength(0)

    act(() => screen.getAllByLabelText('Go to next page')[0].click())

    //Confirm next page view is correct
    expect(screen.queryAllByText('question1')).toHaveLength(0)
    expect(screen.getAllByText('question2')).toHaveLength(2)

    act(() => screen.getAllByLabelText('Go to previous page')[0].click())

    //Confirm returning to the original page view is correct
    expect(screen.getAllByText('question1')).toHaveLength(2)
    expect(screen.queryAllByText('question2')).toHaveLength(0)
  })

  it('should scroll to top', () => {
    renderWithRouter(
      <SplitFormDesigner
        content={{ 'title': 'Foo', 'pages': [{ 'elements': [] }] }}
        onChange={() => jest.fn()}
        currentLanguage={{ id: '0', languageCode: 'en', languageName: 'English' }}
        supportedLanguages={[{ id: '0', languageCode: 'en', languageName: 'English' }]}
      />)
    act(() => screen.getByText('Scroll to top').click())
    expect(window.scrollTo).toHaveBeenCalledWith(0, 0)
  })
})
