import { kcSanitize } from "keycloakify/lib/kcSanitize";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../KcContext";
import type { I18n } from "../i18n";
import { Box, TextField, Button, Typography, Link, Alert, Grid } from "@mui/material";

export function LoginResetPassword(props: PageProps<Extract<KcContext, { pageId: "login-reset-password.ftl" }>, I18n>) {
    const { kcContext, i18n, Template } = props;

    const { url, realm, auth, messagesPerField } = kcContext;

    const { msg, msgStr } = i18n;

    return (
        <Template
            kcContext={kcContext}
            i18n={i18n}
            doUseDefaultCss={false}
            displayInfo
            displayMessage={!messagesPerField.existsError("username")}
            infoNode={<Typography>{realm.duplicateEmailsAllowed ? msg("emailInstructionUsername") : msg("emailInstruction")}</Typography>}
            headerNode={msg("emailForgotTitle")}
        >
            <Box component="form" id="kc-reset-password-form" action={url.loginAction} method="post">
                <Grid container spacing={3}>
                    {/* Username/Email Field */}
                    <Grid size={12}>
                        <TextField
                            fullWidth
                            id="username"
                            name="username"
                            label={!realm.loginWithEmailAllowed
                                ? msg("username")
                                : !realm.registrationEmailAsUsername
                                    ? msg("usernameOrEmail")
                                    : msg("email")
                            }
                            defaultValue={auth.attemptedUsername ?? ""}
                            error={messagesPerField.existsError("username")}
                            autoFocus
                            variant="outlined"
                        />
                        {messagesPerField.existsError("username") && (
                            <Alert severity="error" sx={{ mt: 1 }}>
                                <div dangerouslySetInnerHTML={{
                                    __html: kcSanitize(messagesPerField.get("username"))
                                }} />
                            </Alert>
                        )}
                    </Grid>

                    {/* Submit Button */}
                    <Grid size={12}>
                        <Button
                            type="submit"
                            variant="contained"
                            fullWidth
                            size="large"
                        >
                            {msgStr("doSubmit")}
                        </Button>
                    </Grid>

                    {/* Back to Login Link */}
                    <Grid size={12}>
                        <Box display="flex" justifyContent="left">
                            <Typography>
                                <Link
                                    href={url.loginUrl}
                                    underline="hover"
                                >
                                    {msg("backToLogin")}
                                </Link>
                            </Typography>
                        </Box>
                    </Grid>
                </Grid>
            </Box>
        </Template>
    );
}