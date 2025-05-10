import type { JSX } from "keycloakify/tools/JSX";
import {useEffect, Fragment, cloneElement, Dispatch, ChangeEvent} from "react";
import { assert } from "keycloakify/tools/assert";
import {
    useUserProfileForm,
    getButtonToDisplayForMultivaluedAttributeField,
    type FormAction,
    type FormFieldError
} from "keycloakify/login/lib/useUserProfileForm";
import type { UserProfileFormFieldsProps } from "keycloakify/login/UserProfileFormFieldsProps";
import type { Attribute } from "keycloakify/login/KcContext";
import type { KcContext } from "./KcContext";
import type { I18n } from "./i18n";
import {
    TextField,
    FormControl,
    FormLabel,
    FormHelperText,
    Select,
    MenuItem,
    Checkbox,
    Radio,
    RadioGroup,
    FormControlLabel,
    Button,
    IconButton,
    InputAdornment,
    Stack,
    Box,
    Typography, Grid
} from "@mui/material";
import { Visibility, VisibilityOff } from "@mui/icons-material";
import {useIsPasswordRevealed} from "keycloakify/tools/useIsPasswordRevealed";

export default function UserProfileFormFields(props: UserProfileFormFieldsProps<KcContext, I18n>) {
    const { kcContext, i18n, onIsFormSubmittableValueChange, doMakeUserConfirmPassword, BeforeField, AfterField } = props;

    const { advancedMsg } = i18n;

    const {
        formState: { formFieldStates, isFormSubmittable },
        dispatchFormAction
    } = useUserProfileForm({
        kcContext,
        i18n,
        doMakeUserConfirmPassword
    });

    useEffect(() => {
        onIsFormSubmittableValueChange(isFormSubmittable);
    }, [isFormSubmittable]);

    const groupNameRef = { current: "" };

    return (
        <Box sx={{ width: '100%' }}>
            <Grid container spacing={2}>
                {formFieldStates.map(({ attribute, displayableErrors, valueOrValues }) => (
                    <Grid size={{xs: 12, md: attribute.name === 'firstName' || attribute.name === 'lastName' ? 6 : 12}} key={attribute.name}>
                        <Fragment key={attribute.name}>
                            <GroupLabel attribute={attribute} groupNameRef={groupNameRef} i18n={i18n} />
                            {BeforeField !== undefined && (
                                <BeforeField
                                    attribute={attribute}
                                    dispatchFormAction={dispatchFormAction}
                                    displayableErrors={displayableErrors}
                                    valueOrValues={valueOrValues}
                                    kcClsx={props.kcClsx}
                                    i18n={i18n}
                                />
                            )}
                            <Box
                                sx={{
                                    display: attribute.name === "password-confirm" && !doMakeUserConfirmPassword ? "none" : undefined
                                }}
                            >
                                <FormControl
                                    fullWidth
                                    error={displayableErrors.length > 0}
                                    variant="outlined"
                                >
                                    <FormLabel required={attribute.required} htmlFor={attribute.name}>
                                        {advancedMsg(attribute.displayName ?? "")}
                                    </FormLabel>

                                    {attribute.annotations.inputHelperTextBefore && (
                                        <FormHelperText>
                                            {advancedMsg(attribute.annotations.inputHelperTextBefore)}
                                        </FormHelperText>
                                    )}

                                    <InputFieldByType
                                        attribute={attribute}
                                        valueOrValues={valueOrValues}
                                        displayableErrors={displayableErrors}
                                        dispatchFormAction={dispatchFormAction}
                                        i18n={i18n}
                                    />

                                    <FieldErrors
                                        attribute={attribute}
                                        displayableErrors={displayableErrors}
                                        fieldIndex={undefined}
                                    />

                                    {attribute.annotations.inputHelperTextAfter && (
                                        <FormHelperText>
                                            {advancedMsg(attribute.annotations.inputHelperTextAfter)}
                                        </FormHelperText>
                                    )}

                                    {AfterField && (
                                        <AfterField
                                            attribute={attribute}
                                            dispatchFormAction={dispatchFormAction}
                                            displayableErrors={displayableErrors}
                                            valueOrValues={valueOrValues}
                                            kcClsx={props.kcClsx}
                                            i18n={i18n}
                                        />
                                    )}
                                </FormControl>
                            </Box>
                        </Fragment>
                    </Grid>
                ))}
                {kcContext.locale !== undefined && formFieldStates.find(x => x.attribute.name === "locale") === undefined && (
                    <input type="hidden" name="locale" value={i18n.currentLanguage.languageTag} />
                )}
            </Grid>
        </Box>
    );
}

function GroupLabel(props: {
    attribute: Attribute;
    groupNameRef: { current: string };
    i18n: I18n;
}) {
    const { attribute, groupNameRef, i18n } = props;
    const { advancedMsg } = i18n;

    if (attribute.group?.name !== groupNameRef.current) {
        groupNameRef.current = attribute.group?.name ?? "";

        if (groupNameRef.current !== "") {
            assert(attribute.group !== undefined);

            return (
                <Box {...Object.fromEntries(Object.entries(attribute.group.html5DataAnnotations).map(([key, value]) => [`data-${key}`, value]))}>
                    <Typography variant="h6" id={`header-${attribute.group.name}`}>
                        {attribute.group.displayHeader ? advancedMsg(attribute.group.displayHeader) : attribute.group.name}
                    </Typography>

                    {attribute.group.displayDescription && (
                        <Typography variant="body2" id={`description-${attribute.group.name}`}>
                            {advancedMsg(attribute.group.displayDescription)}
                        </Typography>
                    )}
                </Box>
            );
        }
    }

    return null;
}

function FieldErrors(props: { attribute: Attribute; displayableErrors: FormFieldError[]; fieldIndex: number | undefined }) {
    const { attribute, fieldIndex } = props;

    const displayableErrors = props.displayableErrors.filter(error => error.fieldIndex === fieldIndex);

    if (displayableErrors.length === 0) {
        return null;
    }

    return (
        <FormHelperText
            error
            id={`input-error-${attribute.name}${fieldIndex === undefined ? "" : `-${fieldIndex}`}`}
        >
            {displayableErrors.map(({ errorMessage }, i) => (
                <div key={i}>{errorMessage}</div>
            ))}
        </FormHelperText>
    );
}
type InputFieldByTypeProps = {
    attribute: Attribute;
    valueOrValues: string | string[];
    displayableErrors: FormFieldError[];
    dispatchFormAction: Dispatch<FormAction>;
    i18n: I18n;
};

function InputFieldByType(props: InputFieldByTypeProps) {
    const { attribute, valueOrValues } = props;

    switch (attribute.annotations.inputType) {
        case "textarea":
            return <TextareaTag {...props} />;
        case "select":
        case "multiselect":
            return <SelectTag {...props} />;
        case "select-radiobuttons":
        case "multiselect-checkboxes":
            return <InputTagSelects {...props} />;
        default: {
            if (valueOrValues instanceof Array) {
                return (
                    <Stack spacing={2}>
                        {valueOrValues.map((_, i) => (
                            <InputTag key={i} {...props} fieldIndex={i} />
                        ))}
                    </Stack>
                );
            }

            const inputNode = <InputTag {...props} fieldIndex={undefined} />;

            if (attribute.name === "password" || attribute.name === "password-confirm") {
                return (
                    <PasswordWrapper
                        i18n={props.i18n}
                        passwordInputId={attribute.name}
                    >
                        {inputNode}
                    </PasswordWrapper>
                );
            }

            return inputNode;
        }
    }
}

function PasswordWrapper(props: { i18n: I18n; passwordInputId: string; children: JSX.Element }) {
    const { i18n, passwordInputId, children } = props;
    const { msgStr } = i18n;

    // Use the original hook instead of custom state
    const { isPasswordRevealed, toggleIsPasswordRevealed } = useIsPasswordRevealed({ passwordInputId });

    return (
        <Box sx={{ position: 'relative' }}>
            {cloneElement(children, {
                type: isPasswordRevealed ? 'text' : 'password',
                slotProps: {
                    input: {
                        endAdornment: (
                            <InputAdornment position="end">
                                <IconButton
                                    aria-label={msgStr(isPasswordRevealed ? "hidePassword" : "showPassword")}
                                    onClick={toggleIsPasswordRevealed}
                                    edge="end"
                                >
                                    {isPasswordRevealed ? <VisibilityOff /> : <Visibility />}
                                </IconButton>
                            </InputAdornment>
                        )
                    }
                }
            })}
        </Box>
    );
}

function InputTag({
                      attribute,
                      fieldIndex,
                      dispatchFormAction,
                      valueOrValues,
                      i18n,
                      displayableErrors,
                      // Extract known props to exclude them from being passed to TextField
                      ...additionalProps
                  } : InputFieldByTypeProps & {
    fieldIndex: number | undefined
    [key: string]: any // Allow any additional properties
}) {
    const { advancedMsgStr } = i18n;

    // Determine the value based on field index
    const value = (() => {
        if (fieldIndex !== undefined) {
            assert(valueOrValues instanceof Array);
            return valueOrValues[fieldIndex];
        }

        assert(typeof valueOrValues === "string");
        return valueOrValues;
    })();

    // Determine input type
    const inputType = (() => {
        const { inputType } = attribute.annotations;

        if (inputType?.startsWith("html5-")) {
            return inputType.slice(6);
        }

        return inputType ?? "text";
    })();

    // Handle change event
    const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
        dispatchFormAction({
            action: "update",
            name: attribute.name,
            valueOrValues: (() => {
                if (fieldIndex !== undefined) {
                    assert(valueOrValues instanceof Array);

                    return valueOrValues.map((val, i) => {
                        if (i === fieldIndex) {
                            return event.target.value;
                        }
                        return val;
                    });
                }
                return event.target.value;
            })()
        });
    };

    // Handle blur event
    const handleBlur = () => {
        dispatchFormAction({
            action: "focus lost",
            name: attribute.name,
            fieldIndex: fieldIndex
        });
    };

    const {slotProps, ...textFieldProps} = additionalProps;
    const allSlotProps = {
        htmlInput: {
            maxLength: attribute.annotations.inputTypeMaxlength !== undefined
                ? parseInt(`${attribute.annotations.inputTypeMaxlength}`)
                : undefined,
            minLength: attribute.annotations.inputTypeMinlength !== undefined
                ? parseInt(`${attribute.annotations.inputTypeMinlength}`)
                : undefined,
            max: attribute.annotations.inputTypeMax,
            min: attribute.annotations.inputTypeMin,
            step: attribute.annotations.inputTypeStep,
            pattern: attribute.annotations.inputTypePattern,
        },
        ...slotProps
    }

    return (
        <>
            <TextField
                type={inputType}
                id={attribute.name}
                name={attribute.name}
                value={value}
                variant="outlined"
                fullWidth
                error={displayableErrors.find(error => error.fieldIndex === fieldIndex) !== undefined}
                disabled={attribute.readOnly}
                autoComplete={attribute.autocomplete}
                placeholder={
                    attribute.annotations.inputTypePlaceholder === undefined
                        ? undefined
                        : advancedMsgStr(attribute.annotations.inputTypePlaceholder)
                }
                {...textFieldProps}
                slotProps={allSlotProps}
                onChange={handleChange}
                onBlur={handleBlur}
                sx={{ mt: 1 }}
            />

            {fieldIndex !== undefined && (
                <>
                    <FieldErrors
                        attribute={attribute}
                        displayableErrors={displayableErrors}
                        fieldIndex={fieldIndex}
                    />

                    {valueOrValues instanceof Array && (
                        <AddRemoveButtonsMultiValuedAttribute
                            attribute={attribute}
                            values={valueOrValues}
                            fieldIndex={fieldIndex}
                            dispatchFormAction={dispatchFormAction}
                            i18n={i18n}
                        />
                    )}
                </>
            )}
        </>
    );
}

function AddRemoveButtonsMultiValuedAttribute(props: {
    attribute: Attribute;
    values: string[];
    fieldIndex: number;
    dispatchFormAction: Dispatch<Extract<FormAction, { action: "update" }>>;
    i18n: I18n;
}) {
    const { attribute, values, fieldIndex, dispatchFormAction, i18n } = props;
    const { msg } = i18n;
    const { hasAdd, hasRemove } = getButtonToDisplayForMultivaluedAttributeField({ attribute, values, fieldIndex });
    const idPostfix = `-${attribute.name}-${fieldIndex + 1}`;

    return (
        <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
            {hasRemove && (
                <Button
                    id={`kc-remove${idPostfix}`}
                    variant="text"
                    size="small"
                    onClick={() =>
                        dispatchFormAction({
                            action: "update",
                            name: attribute.name,
                            valueOrValues: values.filter((_, i) => i !== fieldIndex)
                        })
                    }
                >
                    {msg("remove")}
                </Button>
            )}

            {hasAdd && (
                <Button
                    id={`kc-add${idPostfix}`}
                    variant="text"
                    size="small"
                    onClick={() =>
                        dispatchFormAction({
                            action: "update",
                            name: attribute.name,
                            valueOrValues: [...values, ""]
                        })
                    }
                >
                    {msg("addValue")}
                </Button>
            )}
        </Box>
    );
}

function InputTagSelects(props: InputFieldByTypeProps) {
    const { attribute, dispatchFormAction, i18n, valueOrValues } = props;
    const { inputType } = attribute.annotations;

    assert(inputType === "select-radiobuttons" || inputType === "multiselect-checkboxes");

    const isRadio = inputType === "select-radiobuttons";
    const options = getOptions(attribute);

    // Handle change for radio buttons and checkboxes
    const handleChange = (event: ChangeEvent<HTMLInputElement>, option: string) => {
        const isChecked = event.target.checked;

        dispatchFormAction({
            action: "update",
            name: attribute.name,
            valueOrValues: (() => {
                if (isRadio) {
                    return isChecked ? option : "";
                } else {
                    // For checkboxes (multiselect)
                    assert(valueOrValues instanceof Array);
                    const newValues = [...valueOrValues];

                    if (isChecked) {
                        if (!newValues.includes(option)) {
                            newValues.push(option);
                        }
                    } else {
                        const index = newValues.indexOf(option);
                        if (index !== -1) {
                            newValues.splice(index, 1);
                        }
                    }

                    return newValues;
                }
            })()
        });
    };

    // Handle blur
    const handleBlur = () => {
        dispatchFormAction({
            action: "focus lost",
            name: attribute.name,
            fieldIndex: undefined
        });
    };

    if (isRadio) {
        return (
            <RadioGroup
                name={attribute.name}
                value={valueOrValues}
                onChange={(e) => {
                    dispatchFormAction({
                        action: "update",
                        name: attribute.name,
                        valueOrValues: e.target.value
                    });
                }}
                sx={{ mt: 1 }}
            >
                {options.map(option => (
                    <FormControlLabel
                        key={option}
                        value={option}
                        control={
                            <Radio
                                id={`${attribute.name}-${option}`}
                                disabled={attribute.readOnly}
                                onBlur={handleBlur}
                            />
                        }
                        label={inputLabel(i18n, attribute, option)}
                        disabled={attribute.readOnly}
                    />
                ))}
            </RadioGroup>
        );
    } else {
        return (
            <Box sx={{ mt: 1 }}>
                {options.map(option => (
                    <FormControlLabel
                        key={option}
                        control={
                            <Checkbox
                                id={`${attribute.name}-${option}`}
                                name={attribute.name}
                                value={option}
                                disabled={attribute.readOnly}
                                checked={valueOrValues instanceof Array ? valueOrValues.includes(option) : valueOrValues === option}
                                onChange={(e) => handleChange(e, option)}
                                onBlur={handleBlur}
                            />
                        }
                        label={inputLabel(i18n, attribute, option)}
                        disabled={attribute.readOnly}
                    />
                ))}
            </Box>
        );
    }
}

function TextareaTag(props: InputFieldByTypeProps) {
    const { attribute, dispatchFormAction, displayableErrors, valueOrValues } = props;

    assert(typeof valueOrValues === "string");

    return (
        <TextField
            id={attribute.name}
            name={attribute.name}
            value={valueOrValues}
            multiline
            rows={attribute.annotations.inputTypeRows === undefined ? 4 : parseInt(`${attribute.annotations.inputTypeRows}`)}
            variant="outlined"
            fullWidth
            error={displayableErrors.length !== 0}
            disabled={attribute.readOnly}
            inputProps={{
                maxLength: attribute.annotations.inputTypeMaxlength === undefined
                    ? undefined
                    : parseInt(`${attribute.annotations.inputTypeMaxlength}`),
            }}
            onChange={(event) =>
                dispatchFormAction({
                    action: "update",
                    name: attribute.name,
                    valueOrValues: event.target.value
                })
            }
            onBlur={() =>
                dispatchFormAction({
                    action: "focus lost",
                    name: attribute.name,
                    fieldIndex: undefined
                })
            }
            sx={{ mt: 1 }}
        />
    );
}

function SelectTag(props: InputFieldByTypeProps) {
    const { attribute, dispatchFormAction, displayableErrors, i18n, valueOrValues } = props;

    const isMultiple = attribute.annotations.inputType === "multiselect";
    const options = getOptions(attribute);

    return (
        <FormControl
            fullWidth
            error={displayableErrors.length !== 0}
            sx={{ mt: 1 }}
        >
            <Select
                id={attribute.name}
                name={attribute.name}
                value={valueOrValues}
                multiple={isMultiple}
                disabled={attribute.readOnly}
                size="small"
                onChange={(event) =>
                    dispatchFormAction({
                        action: "update",
                        name: attribute.name,
                        valueOrValues: event.target.value
                    })
                }
                onBlur={() =>
                    dispatchFormAction({
                        action: "focus lost",
                        name: attribute.name,
                        fieldIndex: undefined
                    })
                }
                displayEmpty
            >
                {!isMultiple && <MenuItem value=""><em>Select...</em></MenuItem>}
                {options.map(option => (
                    <MenuItem key={option} value={option}>
                        {inputLabel(i18n, attribute, option)}
                    </MenuItem>
                ))}
            </Select>
        </FormControl>
    );
}

// Helper function to get options
function getOptions(attribute: Attribute): string[] {
    // Try to get options from validator
    if (attribute.annotations.inputOptionsFromValidation) {
        const validator = (attribute.validators as Record<string, { options?: string[] }>)
            [attribute.annotations.inputOptionsFromValidation];

        if (validator?.options) {
            return validator.options;
        }
    }

    // Fallback to options validator
    return attribute.validators.options?.options ?? [];
}

function inputLabel(i18n: I18n, attribute: Attribute, option: string) {
    const { advancedMsg } = i18n;

    if (attribute.annotations.inputOptionLabels) {
        return advancedMsg(attribute.annotations.inputOptionLabels[option] ?? option);
    }

    if (attribute.annotations.inputOptionLabelsI18nPrefix) {
        return advancedMsg(`${attribute.annotations.inputOptionLabelsI18nPrefix}.${option}`);
    }

    return option;
}