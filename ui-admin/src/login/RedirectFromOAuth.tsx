import React, {useEffect} from 'react'
import {useMsal} from "@azure/msal-react";
import {EventType, PublicClientApplication} from "@azure/msal-browser";
import {EventMessage} from "@azure/msal-browser/dist/event/EventMessage";
import {AuthenticationResult} from "@azure/msal-common";
import { useUser } from "user/UserProvider";
import {Navigate, useNavigate} from "react-router-dom";

export const RedirectFromOAuth = () => {
  const { instance } = useMsal()
  const { user, loginUser } = useUser()
  const navigate = useNavigate()

  useEffect(() => {
    const callbackId = instance.addEventCallback((event: EventMessage) => {
      if (event.eventType === EventType.LOGIN_SUCCESS || event.eventType === EventType.ACQUIRE_TOKEN_SUCCESS) {
        const result = event.payload as AuthenticationResult
        console.log('result', result)
        if (result.account) {
          const user = { username: result.account.idTokenClaims?.email as string, token: result.idToken };
          console.log('user', user)
          // TODO: figure out what needs to be in User for loginUser to be able to authenticate a token
          loginUser(user)
          // TODO: after successful sign-in, re-route the user back home or to wherever they were trying to go
          // navigate('/')
        }
      }
    })
    return () => {
      if (callbackId) {
        instance.removeEventCallback(callbackId)
      }
    }
  }, [instance, loginUser, navigate])

  return <div>User email: {user.email}</div>
}