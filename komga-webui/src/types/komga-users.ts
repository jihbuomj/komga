interface UserDto {
  id: number,
  email: string,
  roles: string[]
}

interface UserWithSharedLibrariesDto {
  id: number,
  email: string,
  roles: string[],
  sharedAllLibraries: boolean,
  sharedLibraries: SharedLibraryDto[]
}

interface SharedLibraryDto {
  id: string
}

interface UserCreationDto {
  email: string,
  roles: string[]
}

interface PasswordUpdateDto {
  password: string
}

interface SharedLibrariesUpdateDto {
  all: boolean,
  libraryIds: string[]
}

interface RolesUpdateDto {
  roles: string[]
}
