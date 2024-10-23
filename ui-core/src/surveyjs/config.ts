import {
  Question,
  Serializer
} from 'survey-core'

Serializer.addProperty('survey', { name: 'questionTemplates', category: 'general' })

Serializer.addProperty('question', { name: 'questionTemplateName', category: 'general' })

// we need a custom "none" value on some questions because some of our "none" are "prefer not to answer"
// see https://github.com/surveyjs/survey-library/issues/5459
Serializer.addProperty('selectbase', {
  name: 'noneValue',
  dependsOn: 'showNoneItem',
  visibleIf: (obj: Question) => {
    return obj.hasNone
  },
  nextToProperty: 'showNoneItem',
  onGetValue: (obj: Question) => {
    return !!obj && !!obj.noneItem ? obj.noneItem.value : 'none'
  },
  onSetValue: (obj: Question, val: string) => {
    obj.noneItem.value = val
  }
})
