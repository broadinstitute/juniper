import { Dispatch, SetStateAction, useId } from 'react'

/**
 * helper function for setting up an accessible react-select component, returns the currently selected item, and a
 * set of params to pass into the select component
 *
 * onChange: pass to the onChange of the <Select>
 * options: pass to the options of the <Select>
 * selectedItem: current value, use in your component logic
 * selectedOption: pass to the "value" of the <Select>
 * selectInputId: pass ot the of the <Select>
 *
 * */
export default function useReactSingleSelect<T>(items: T[],
  labelFunction: (i: T) => {label: React.ReactNode, value: T},
  setSelectedItem: Dispatch<SetStateAction<T | undefined>>, selectedItem?: T) {
  const options = items.map(labelFunction)
  const selectedValue = selectedItem ? labelFunction(selectedItem).value : undefined
  const selectedOption = options.find(opt => opt.value === selectedValue)
  const selectInputId = useId()

  const onChange = (opt: {label: React.ReactNode, value: T} | null) => setSelectedItem(opt?.value)
  return { onChange, options, selectedOption, selectInputId }
}
