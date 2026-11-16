import { createContext, useContext } from "react"

export const AccountContext = createContext({ user: null, setUser: () => {} })
export const useAccount = () => useContext(AccountContext)
