
import { Suspense, lazy } from "react";
import type { ClassKey } from "keycloakify/login";
import type { KcContext } from "./KcContext";
import { useI18n } from "./i18n";
import DefaultPage from "keycloakify/login/DefaultPage";
import {Template} from "./Template";
import { ThemeProvider } from "@mui/material";
import { Login } from "./pages/Login";
import {theme} from "./Theme.ts";
import {Register} from "@/login/pages/Register.tsx";

const UserProfileFormFields = lazy(
    () => import("./UserProfileFormFields")
);

const doMakeUserConfirmPassword = true;

export default function KcPage(props: { kcContext: KcContext }) {
    const { kcContext } = props;
    const { i18n } = useI18n({ kcContext });

    return (
        <ThemeProvider theme={theme}>
            <Suspense>
                {(() => {
                    switch (kcContext.pageId) {
                        case 'login.ftl':
                            return (
                              <Login
                                kcContext={kcContext}
                                i18n={i18n}
                                classes={classes}
                                Template={Template}
                                doUseDefaultCss={false}
                              />
                            );
                        case 'register.ftl':
                            return <Register kcContext={kcContext}
                                             i18n={i18n}
                                             classes={classes}
                                             Template={Template}
                                             doUseDefaultCss={false}
                                             doMakeUserConfirmPassword={true}
                                             UserProfileFormFields={UserProfileFormFields}
                            />
                        default:
                            return (
                                <DefaultPage
                                    kcContext={kcContext}
                                    i18n={i18n}
                                    classes={classes}
                                    Template={Template}
                                    doUseDefaultCss={false}
                                    UserProfileFormFields={UserProfileFormFields}
                                    doMakeUserConfirmPassword={doMakeUserConfirmPassword}
                                />
                            );
                    }
                })()}
            </Suspense>
        </ThemeProvider>
    );
}

const classes = {
    kcFormGroupClass: "MuiFormControl-root",
    kcInputClass: "MuiOutlinedInput-input",
    kcButtonClass: "MuiButton-root",
    // Add more class mappings as needed
} satisfies { [key in ClassKey]?: string };