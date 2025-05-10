import { useState } from "react";
import { kcSanitize } from "keycloakify/lib/kcSanitize";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../KcContext";
import type { I18n } from "../i18n";
import {
    TextField,
    Button,
    FormControl,
    FormControlLabel,
    Checkbox,
    Typography,
    Link,
    Divider,
    IconButton,
    InputAdornment,
    Box
} from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import {
    Google,
    Facebook,
    Instagram,
    Twitter,
    LinkedIn,
    GitHub,
    CloudQueue,
    Payment,
    Microsoft,
    Code
} from '@mui/icons-material';
import { SvgIcon } from '@mui/material';

const socialIconMap: Record<string, typeof SvgIcon> = {
    google: Google,
    microsoft: Microsoft,
    facebook: Facebook,
    instagram: Instagram,
    twitter: Twitter,
    linkedin: LinkedIn,
    stackoverflow: Code, // Using Code as a close alternative
    github: GitHub,
    gitlab: Code, // Using Code as alternative
    bitbucket: Code, // Using Code as alternative
    paypal: Payment,
    openshift: CloudQueue // Using CloudQueue for cloud platform
};


export function Login(props: PageProps<Extract<KcContext, { pageId: "login.ftl" }>, I18n>) {
    const { kcContext, i18n, doUseDefaultCss, Template, classes } = props;

    const { social, realm, url, usernameHidden, login, auth, registrationDisabled, messagesPerField } = kcContext;

    const { msg, msgStr } = i18n;

    const [isLoginButtonDisabled, setIsLoginButtonDisabled] = useState(false);
    const [showPassword, setShowPassword] = useState(false);

    const handleClickShowPassword = () => setShowPassword(!showPassword);

    const getSocialIcon = (alias: string) => {
        const Icon = socialIconMap[alias.toLowerCase()];
        return Icon ? <Icon /> : null;
    };

    return (
        <Template
            kcContext={kcContext}
            i18n={i18n}
            doUseDefaultCss={doUseDefaultCss}
            classes={classes}
            displayMessage={!messagesPerField.existsError("username", "password")}
            headerNode={<Typography variant="h5">{msg("loginAccountTitle")}</Typography>}
            displayInfo={realm.password && realm.registrationAllowed && !registrationDisabled}
            infoNode={
                <Box sx={{ mt: 2 }}>
                    <Typography>
                        {msg("noAccount")}{" "}
                        <Link href={url.registrationUrl} tabIndex={8}>
                            {msg("doRegister")}
                        </Link>
                    </Typography>
                </Box>
            }
            socialProvidersNode={
                <>
                    {realm.password && social?.providers !== undefined && social.providers.length !== 0 && (
                        <Box sx={{ mt: 3 }}>
                            <Divider sx={{ my: 2 }} />
                            <Typography variant="h6">{msg("identity-provider-login-label")}</Typography>
                            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, mt: 2 }}>
                                {social.providers.map(p => (
                                    <Button
                                        key={p.alias}
                                        variant="outlined"
                                        href={p.loginUrl}
                                        startIcon={getSocialIcon(p.alias)}
                                    >
                                        <span dangerouslySetInnerHTML={{ __html: kcSanitize(p.displayName) }} />
                                    </Button>
                                ))}
                            </Box>
                        </Box>
                    )}
                </>
            }
        >

            <Box component="div">
                {realm.password && (
                    <form
                        onSubmit={() => {
                            setIsLoginButtonDisabled(true);
                            return true;
                        }}
                        action={url.loginAction}
                        method="post"
                    >
                        {!usernameHidden && (
                            <FormControl fullWidth sx={{ mb: 2 }}>
                                <TextField
                                    label={!realm.loginWithEmailAllowed
                                        ? msg("username")
                                        : !realm.registrationEmailAsUsername
                                            ? msg("usernameOrEmail")
                                            : msg("email")}
                                    id="username"
                                    name="username"
                                    defaultValue={login.username ?? ""}
                                    autoComplete="username"
                                    autoFocus
                                    error={messagesPerField.existsError("username", "password")}
                                    helperText={messagesPerField.existsError("username", "password")
                                        ? messagesPerField.getFirstError("username", "password")
                                        : ""}
                                    tabIndex={2}
                                />
                            </FormControl>
                        )}

                        <FormControl fullWidth sx={{ mb: 2 }}>
                            <TextField
                                label={msg("password")}
                                id="password"
                                name="password"
                                type={showPassword ? 'text' : 'password'}
                                autoComplete="current-password"
                                error={messagesPerField.existsError("username", "password")}
                                helperText={messagesPerField.existsError("username", "password")
                                    ? messagesPerField.getFirstError("username", "password")
                                    : ""}
                                tabIndex={3}
                                slotProps={{
                                    input: {
                                        endAdornment: (
                                            <InputAdornment position="end">
                                                <IconButton
                                                    aria-label={msgStr(showPassword ? "hidePassword" : "showPassword")}
                                                    onClick={handleClickShowPassword}
                                                    edge="end"
                                                >
                                                    {showPassword ? <VisibilityOff /> : <Visibility />}
                                                </IconButton>
                                            </InputAdornment>
                                        ),
                                    }
                                }}
                            />
                        </FormControl>

                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                            {realm.rememberMe && !usernameHidden && (
                                <FormControlLabel
                                    control={
                                        <Checkbox
                                            id="rememberMe"
                                            name="rememberMe"
                                            defaultChecked={!!login.rememberMe}
                                            tabIndex={5}
                                        />
                                    }
                                    label={msg("rememberMe")}
                                />
                            )}
                            {realm.resetPasswordAllowed && (
                                <Typography>
                                    <Link href={url.loginResetCredentialsUrl} tabIndex={6}>
                                        {msg("doForgotPassword")}
                                    </Link>
                                </Typography>
                            )}
                        </Box>

                        <input type="hidden" id="id-hidden-input" name="credentialId" value={auth.selectedCredential} />
                        <Button
                            type="submit"
                            variant="contained"
                            fullWidth
                            disabled={isLoginButtonDisabled}
                            tabIndex={7}
                        >
                            {msgStr("doLogIn")}
                        </Button>
                    </form>
                )}
            </Box>
        </Template>
    );
}