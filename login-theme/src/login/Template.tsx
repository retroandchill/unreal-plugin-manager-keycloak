import {useEffect} from "react";
import {clsx} from "keycloakify/tools/clsx";
import {kcSanitize} from "keycloakify/lib/kcSanitize";
import type {TemplateProps} from "keycloakify/login/TemplateProps";
import {getKcClsx} from "keycloakify/login/lib/kcClsx";
import {useSetClassName} from "keycloakify/tools/useSetClassName";
import {useInitialize} from "keycloakify/login/Template.useInitialize";
import type {I18n} from "./i18n";
import type {KcContext} from "./KcContext";
import {
    Box,
    Paper,
    Typography,
    Container,
    AppBar,
    Toolbar,
    Select,
    MenuItem,
    FormControl,
    Button,
    Alert,
    Stack,
} from "@mui/material";
import {
    Check as CheckIcon,
    Warning as WarningIcon,
    Error as ErrorIcon,
    Info as InfoIcon,
    Refresh as RefreshIcon
} from "@mui/icons-material";
import {theme} from "./Theme.ts";

export function Template(props: TemplateProps<KcContext, I18n>) {
    const {
        displayInfo = false,
        displayMessage = true,
        displayRequiredFields = false,
        headerNode,
        socialProvidersNode = null,
        infoNode = null,
        documentTitle,
        bodyClassName,
        kcContext,
        i18n,
        doUseDefaultCss,
        classes,
        children
    } = props;

    const {kcClsx} = getKcClsx({doUseDefaultCss, classes});

    const {msg, msgStr, currentLanguage, enabledLanguages} = i18n;

    const {realm, auth, url, message, isAppInitiatedAction} = kcContext;

    useEffect(() => {
        document.title = documentTitle ?? msgStr("loginTitle", realm.displayName);
    }, []);

    useSetClassName({
        qualifiedName: "html",
        className: kcClsx("kcHtmlClass")
    });

    useSetClassName({
        qualifiedName: "body",
        className: bodyClassName ?? kcClsx("kcBodyClass")
    });

    const {isReadyToRender} = useInitialize({kcContext, doUseDefaultCss});

    if (!isReadyToRender) {
        return null;
    }

    return (
        <Box
            sx={{
                backgroundColor: theme.palette.background.default,
                minHeight: "100vh",
                width: "100%",
            }}
        >
            <Container maxWidth="sm">
                <Box
                    sx={{
                        minHeight: "100vh",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                    }}
                >
                    <Paper elevation={3} className={kcClsx("kcLoginClass")} sx={{p: 3}}>
                        <AppBar position="static" color="primary" id="kc-header" className={kcClsx("kcHeaderClass")}
                                sx={{mb: 3}}>
                            <Toolbar>
                                <Typography variant="h6" component="div" id="kc-header-wrapper"
                                            className={kcClsx("kcHeaderWrapperClass")}>
                                    <div
                                        dangerouslySetInnerHTML={{__html: msgStr("loginTitleHtml", realm.displayNameHtml)}}/>
                                </Typography>
                            </Toolbar>
                        </AppBar>

                        <Box className={kcClsx("kcFormCardClass")}>
                            <Box component="header" className={kcClsx("kcFormHeaderClass")} sx={{mb: 3}}>
                                {enabledLanguages.length > 1 && (
                                    <Box className={kcClsx("kcLocaleMainClass")} id="kc-locale" sx={{mb: 2}}>
                                        <FormControl
                                            fullWidth
                                            id="kc-locale-wrapper"
                                            className={kcClsx("kcLocaleWrapperClass")}
                                            size="small"
                                        >
                                            <Select
                                                id="kc-current-locale-link"
                                                value={currentLanguage.languageTag}
                                                displayEmpty
                                                sx={{minWidth: 120}}
                                                MenuProps={{
                                                    id: "language-switch1",
                                                    "aria-labelledby": "kc-current-locale-link"
                                                }}
                                            >
                                                {enabledLanguages.map(({languageTag, label, href}, i) => (
                                                    <MenuItem
                                                        key={languageTag}
                                                        component="a"
                                                        href={href}
                                                        id={`language-${i + 1}`}
                                                        className={kcClsx("kcLocaleItemClass")}
                                                    >
                                                        {label}
                                                    </MenuItem>
                                                ))}
                                            </Select>
                                        </FormControl>
                                    </Box>
                                )}
                                {(() => {
                                    const node = !(auth !== undefined && auth.showUsername && !auth.showResetCredentials) ? (
                                        <Typography variant="h5" component="h1" id="kc-page-title">
                                            {headerNode}
                                        </Typography>
                                    ) : (
                                        <Box id="kc-username" className={kcClsx("kcFormGroupClass")}
                                             sx={{display: 'flex', alignItems: 'center'}}>
                                            <Typography component="label" id="kc-attempted-username">
                                                {auth.attemptedUsername}
                                            </Typography>
                                            <Button
                                                id="reset-login"
                                                component="a"
                                                href={url.loginRestartFlowUrl}
                                                aria-label={msgStr("restartLoginTooltip")}
                                                startIcon={<RefreshIcon/>}
                                                sx={{ml: 2}}
                                                size="small"
                                                title={msgStr("restartLoginTooltip")}
                                            >
                                                {msg("restartLoginTooltip")}
                                            </Button>
                                        </Box>
                                    );

                                    if (displayRequiredFields) {
                                        return (
                                            <Box className={kcClsx("kcContentWrapperClass")}>
                                                <Box className={clsx(kcClsx("kcLabelWrapperClass"))} sx={{mb: 2}}>
                                                    <Typography variant="subtitle2">
                                                        <span className="required">*</span>
                                                        {msg("requiredFields")}
                                                    </Typography>
                                                </Box>
                                                <Box>{node}</Box>
                                            </Box>
                                        );
                                    }

                                    return node;
                                })()}
                            </Box>

                            <Box id="kc-content">
                                <Stack spacing={2} id="kc-content-wrapper">
                                    {/* App-initiated actions should not see warning messages about the need to complete the action during login. */}
                                    {displayMessage && message !== undefined && (message.type !== "warning" || !isAppInitiatedAction) && (
                                        <Alert
                                            severity={message.type === "error" ? "error" :
                                                message.type === "warning" ? "warning" :
                                                    message.type === "success" ? "success" : "info"}
                                            icon={
                                                message.type === "success" ? <CheckIcon/> :
                                                    message.type === "warning" ? <WarningIcon/> :
                                                        message.type === "error" ? <ErrorIcon/> : <InfoIcon/>
                                            }
                                            className={kcClsx("kcAlertClass")}
                                        >
                                    <span
                                        dangerouslySetInnerHTML={{
                                            __html: kcSanitize(message.summary)
                                        }}
                                    />
                                        </Alert>
                                    )}
                                    {children}
                                    {auth !== undefined && auth.showTryAnotherWayLink && (
                                        <Box component="form" id="kc-select-try-another-way-form"
                                             action={url.loginAction} method="post">
                                            <input type="hidden" name="tryAnotherWay" value="on"/>
                                            <Button
                                                component="a"
                                                href="#"
                                                id="try-another-way"
                                                variant="text"
                                                onClick={() => {
                                                    document.forms["kc-select-try-another-way-form" as never].submit();
                                                    return false;
                                                }}
                                            >
                                                {msg("doTryAnotherWay")}
                                            </Button>
                                        </Box>
                                    )}
                                    {socialProvidersNode}
                                    {displayInfo && (
                                        <Box id="kc-info" className={kcClsx("kcSignUpClass")} sx={{mt: 2}}>
                                            <Box id="kc-info-wrapper" className={kcClsx("kcInfoAreaWrapperClass")}>
                                                {infoNode}
                                            </Box>
                                        </Box>
                                    )}
                                </Stack>
                            </Box>
                        </Box>
                    </Paper>
                </Box>
            </Container>
        </Box>
    );
}
