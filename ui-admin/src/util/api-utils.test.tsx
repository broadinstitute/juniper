import { escapeCsvValue } from './downloadUtils'
import {useLoadingEffect} from "./api-utils";
import {useState} from "react";
import {render, screen, waitFor} from "@testing-library/react";

const LoadingTestComponent = () => {
    const [loadedList, setLoadedList] = useState<string[]>([])
    const {isLoading} = useLoadingEffect(async () => {
        const response = await Promise.resolve(['item1'])
        setLoadedList(response)
    })
    return <div>
        {!isLoading && <ul>
            {loadedList.map(item => <li key={item}>{item}</li>)}
        </ul>}
        {isLoading && <span>LOADING</span>}
    </div>
}

describe('useLoadingEffect handles loading state', () => {
    it('manages isLoading', async () => {
        render(<LoadingTestComponent/>)
        expect(screen.getByText('LOADING')).toBeInTheDocument()
        await waitFor(() => expect(screen.getByText('item1')).toBeInTheDocument())
    })
})
